package ca.fraggergames.ffxivextract.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.models.SCD_File;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

public class OggVorbisPlayer {

	// JOGG
	private Packet joggPacket = new Packet();
	private Page joggPage = new Page();
	private StreamState joggStreamState = new StreamState();
	private SyncState joggSyncState = new SyncState();

	// JORBIS
	private DspState jorbisDspState = new DspState();
	private Block jorbisBlock = new Block(jorbisDspState);
	private Comment jorbisComment = new Comment();
	private Info jorbisInfo = new Info();

	// DATA
	private InputStream inStream;

	// BUFFER
	private int bufferSize = 2048;
	private byte[] buffer;
	private int count = 0;
	private int index = 0;

	// SOUND
	private byte[] convertedBuffer;
	private int convertedBufferSize;
	private SourceDataLine outputLine = null;
	private float[][][] pcmInfo;
	private int[] pcmIndex;

	// OTHER
	private boolean isPaused = false;
	private boolean isStopped = false;
	
	public OggVorbisPlayer(int bufferSize) {
		if (bufferSize > 0)
			this.bufferSize = bufferSize;
	}

	public void setSource(SCD_File file) {
		inStream = new ByteArrayInputStream(file.getData());
	}
	
	public void start()
	{
		if (inStream == null)
			return;
		
		readHeader();
		initSoundSystem();		
	}
	
	public void pause(boolean f)
	{
		isPaused = f;
	}

	public boolean isPaused()
	{
		return isPaused;
	}
	
	public void reset()
	{
		isStopped = true;
		joggStreamState.clear();
		jorbisBlock.clear();
		jorbisDspState.clear();
		jorbisInfo.clear();
		joggSyncState.reset();
		joggSyncState.buffer(bufferSize);
		buffer = joggSyncState.data;
				
		outputLine.close();
	}
	
	public void playThreaded(){
		new Thread(){@Override
		public void run() {
			readBody();
			isStopped = false;
		}}.start();		
	}
	
	public boolean initSoundSystem() {

		// This buffer is used by the decoding method.
		convertedBufferSize = bufferSize * 2;
		convertedBuffer = new byte[convertedBufferSize];

		// Initializes the DSP synthesis.
		jorbisDspState.synthesis_init(jorbisInfo);

		// Make the Block object aware of the DSP.
		jorbisBlock.init(jorbisDspState);

		// Wee need to know the channels and rate.
		int channels = jorbisInfo.channels;
		int rate = jorbisInfo.rate;

		// Creates an AudioFormat object and a DataLine.Info object.
		AudioFormat audioFormat = new AudioFormat((float) rate, 16, channels,
				true, false);
		DataLine.Info datalineInfo = new DataLine.Info(SourceDataLine.class,
				audioFormat, AudioSystem.NOT_SPECIFIED);

		// Check if the line is supported.
		if (!AudioSystem.isLineSupported(datalineInfo)) {
			System.err.println("Audio output line is not supported.");
			return false;
		}

		/*
		 * Everything seems to be alright. Let's try to open a line with the
		 * specified format and start the source data line.
		 */
		try {
			outputLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
			outputLine.open(audioFormat);
		} catch (LineUnavailableException exception) {
			System.out.println("The audio output line could not be opened due "
					+ "to resource restrictions.");
			System.err.println(exception);
			return false;
		} catch (IllegalStateException exception) {
			System.out.println("The audio output line is already open.");
			System.err.println(exception);
			return false;
		} catch (SecurityException exception) {
			System.out.println("The audio output line could not be opened due "
					+ "to security restrictions.");
			System.err.println(exception);
			return false;
		}

		// Start it.
		outputLine.start();		

		/*
		 * We create the PCM variables. The index is an array with the same
		 * length as the number of audio channels.
		 */
		pcmInfo = new float[1][][];
		pcmIndex = new int[jorbisInfo.channels];

		return true;
	}

	public void run()
    {
        if(inStream == null)        
            return;
        
        init();
      
        if(readHeader())
        {
            if(initSoundSystem())            
                readBody();            
        }

        finish();
    }
	
	private void readBody() {
		boolean needMoreData = true;

		while (needMoreData && !isStopped) {
			
			if (isPaused)
				continue;
			
			switch (joggSyncState.pageout(joggPage)) {
			case -1:
			case 0:
				break;
			case 1:
				// Give the page to the StreamState object.
				joggStreamState.pagein(joggPage);

				// If granulepos() returns "0", we don't need more data.
				if (joggPage.granulepos() == 0) {
					needMoreData = false;
					break;
				}

				// Here is where we process the packets.
				processPackets: while (true) {
					switch (joggStreamState.packetout(joggPacket)) {
					// Is it a hole in the data?
					case -1:
					case 0:
						break processPackets;					
					case 1:
						decodeCurrentPacket();

					}
				}

				/*
				 * If the page is the end-of-stream, we don't need more data.
				 */
				if (joggPage.eos() != 0)
					needMoreData = false;
			}

			// If we need more data
			if (needMoreData) {
				// We get the new index and an updated buffer.
				index = joggSyncState.buffer(bufferSize);
				buffer = joggSyncState.data;

				// Read from the InputStream.
				try {
					count = inStream.read(buffer, index, bufferSize);
				} catch (Exception e) {
					System.err.println(e);
					return;
				}

				// We let SyncState know how many bytes we read.
				joggSyncState.wrote(count);

				// There's no more data in the stream.
				if (count == 0)
					needMoreData = false;
			}
		}
	}
	
	private void decodeCurrentPacket()
    {
        int samples;

        // Check that the packet is a audio data packet etc.
        if(jorbisBlock.synthesis(joggPacket) == 0)
        {
            // Give the block to the DspState object.
            jorbisDspState.synthesis_blockin(jorbisBlock);
        }

        // We need to know how many samples to process.
        int range;

        /*
         * Get the PCM information and count the samples. And while these
         * samples are more than zero...
         */
        while((samples = jorbisDspState.synthesis_pcmout(pcmInfo, pcmIndex))
            > 0)
        {
            // We need to know for how many samples we are going to process.
            if(samples < convertedBufferSize)
            {
                range = samples;
            }
            else
            {
                range = convertedBufferSize;
            }

            // For each channel...
            for(int i = 0; i < jorbisInfo.channels; i++)
            {
                int sampleIndex = i * 2;

                // For every sample in our range...
                for(int j = 0; j < range; j++)
                {
                    /*
                     * Get the PCM value for the channel at the correct
                     * position.
                     */
                    int value = (int) (pcmInfo[0][i][pcmIndex[i] + j] * 32767);

                    /*
                     * We make sure our value doesn't exceed or falls below
                     * +-32767.
                     */
                    if(value > 32767)
                    {
                        value = 32767;
                    }
                    if(value < -32768)
                    {
                        value = -32768;
                    }

                    /*
                     * It the value is less than zero, we bitwise-or it with
                     * 32768 (which is 1000000000000000 = 10^15).
                     */
                    if(value < 0) value = value | 32768;

                    /*
                     * Take our value and split it into two, one with the last
                     * byte and one with the first byte.
                     */
                    convertedBuffer[sampleIndex] = (byte) (value);
                    convertedBuffer[sampleIndex + 1] = (byte) (value >>> 8);

                    /*
                     * Move the sample index forward by two (since that's how
                     * many values we get at once) times the number of channels.
                     */
                    sampleIndex += 2 * (jorbisInfo.channels);
                }
            }

            // Write the buffer to the audio output line.
            outputLine.write(convertedBuffer, 0, 2 * jorbisInfo.channels
                * range);

            // Update the DspState object.
            jorbisDspState.synthesis_read(range);
        }
    }

	private boolean readHeader() {
		boolean needMoreData = true;
		int packet = 1;

		while (needMoreData) {
			// Read from the InputStream.
			try {
				count = inStream.read(buffer, index, bufferSize);
			} catch (IOException exception) {
				System.err.println("Could not read from the input stream.");
				System.err.println(exception);
			}

			joggSyncState.wrote(count);

			switch (packet) {
			case 1: {
				switch (joggSyncState.pageout(joggPage)) {
				// If there is a hole in the data, we must exit.
				case -1: {
					System.err.println("There is a hole in the first "
							+ "packet data.");
					return false;
				}

				// If we need more data, we break to get it.
				case 0: {
					break;
				}

				/*
				 * We got where we wanted. We have successfully read the first
				 * packet, and we will now initialize and reset StreamState, and
				 * initialize the Info and Comment objects. Afterwards we will
				 * check that the page doesn't contain any errors, that the
				 * packet doesn't contain any errors and that it's Vorbis data.
				 */
				case 1: {
					// Initializes and resets StreamState.
					joggStreamState.init(joggPage.serialno());
					joggStreamState.reset();

					// Initializes the Info and Comment objects.
					jorbisInfo.init();
					jorbisComment.init();

					// Check the page (serial number and stuff).
					if (joggStreamState.pagein(joggPage) == -1) {
						System.err.println("We got an error while "
								+ "reading the first header page.");
						return false;
					}

					/*
					 * Try to extract a packet. All other return values than "1"
					 * indicates there's something wrong.
					 */
					if (joggStreamState.packetout(joggPacket) != 1) {
						System.err.println("We got an error while "
								+ "reading the first header packet.");
						return false;
					}

					/*
					 * We give the packet to the Info object, so that it can
					 * extract the Comment-related information, among other
					 * things. If this fails, it's not Vorbis data.
					 */
					if (jorbisInfo
							.synthesis_headerin(jorbisComment, joggPacket) < 0) {
						System.err.println("We got an error while "
								+ "interpreting the first packet. "
								+ "Apparantly, it's not Vorbis data.");
						return false;
					}
					packet++;
					break;
				}
				}

				if (packet == 1)
					break;
			}

			// The code for the second and third packets follow.
			case 2:
			case 3:
			case 4:
				switch (joggSyncState.pageout(joggPage)) {
				// If there is a hole in the data, we must exit.
				case -1: {
					System.err.println("There is a hole in the second "
							+ "or third packet data.");
					return false;
				}

				// If we need more data, we break to get it.
				case 0: {
					break;
				}

				/*
				 * Here is where we take the page, extract a packet and and (if
				 * everything goes well) give the information to the Info and
				 * Comment objects like we did above.
				 */
				case 1:
					joggStreamState.pagein(joggPage);

					switch (joggStreamState.packetout(joggPacket)) {
					case -1:
						return false;
					case 0:
						break;
					case 1:
						jorbisInfo
								.synthesis_headerin(jorbisComment, joggPacket);

						// Increment packet.
						packet++;

						if (packet == 5)
							needMoreData = false;
						break;
					}
					break;
				}
				break;
			}

			// We get the new index and an updated buffer.
			index = joggSyncState.buffer(bufferSize);
			buffer = joggSyncState.data;

			if (count == 0 && needMoreData) {
				System.err.println("Not enough header data was supplied.");
				return false;
			}
		}

		return true;
	}

	public void init() {
		if (Constants.DEBUG)
			System.out.println("Initializing Jorbis");
		joggSyncState.init();		
		joggSyncState.buffer(bufferSize);
		buffer = joggSyncState.data;
		if (Constants.DEBUG)
			System.out.println("Jorbis Ready");

	}

	public void finish() {
		if (Constants.DEBUG)
			System.out.println("Clearing Jorbis");

		joggStreamState.clear();
		jorbisBlock.clear();
		jorbisDspState.clear();
		jorbisInfo.clear();
		joggSyncState.clear();
		outputLine.close();
		
		try {
			if (inStream != null)
				inStream.close();
		} catch (Exception e) {
		}

		if (Constants.DEBUG)
			System.out.println("Jorbis Cleared");
	}

}
