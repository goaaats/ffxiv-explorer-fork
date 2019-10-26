package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.FileTools;
import com.fragmenterworks.ffxivextract.helpers.SparseArray;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntry;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntryType;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntryType_Frame;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntryType_Graphics;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntryType_List;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntryType_Scrollbar;
import com.fragmenterworks.ffxivextract.models.uldStuff.COHDEntryType_Slider;
import com.fragmenterworks.ffxivextract.models.uldStuff.GraphicsNodeTypeData;
import com.fragmenterworks.ffxivextract.models.uldStuff.GraphicsNodeTypeData_1;
import com.fragmenterworks.ffxivextract.models.uldStuff.GraphicsNodeTypeData_2;
import com.fragmenterworks.ffxivextract.models.uldStuff.GraphicsNodeTypeData_3;
import com.fragmenterworks.ffxivextract.models.uldStuff.GraphicsNodeTypeData_4;
import com.fragmenterworks.ffxivextract.models.uldStuff.ImageSet;
import com.fragmenterworks.ffxivextract.models.uldStuff.TLHDSet;
import com.fragmenterworks.ffxivextract.models.uldStuff.WDHDEntry;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by Roze on 2017-06-17.
 *
 * @author Roze
 */
public class ULD_File extends Game_File {

	/**
	 * List of parsers for graphical nodes
	 */
	private static SparseArray<Class<? extends GraphicsNodeTypeData>> graphicsTypes = new SparseArray<>();

	/**
	 * List of parsers for COHD nodes
	 */
	private static SparseArray<Class<? extends COHDEntryType>> cohdTypes = new SparseArray<>();

	/**
	 * Initialize default parsers
	 */
	static {
		putGraphicsType(1, GraphicsNodeTypeData_1.class);
		putGraphicsType(2, GraphicsNodeTypeData_2.class);
		putGraphicsType(3, GraphicsNodeTypeData_3.class);
		putGraphicsType(4, GraphicsNodeTypeData_4.class);

		putCOHDType(1, COHDEntryType_Graphics.class);
		putCOHDType(2, COHDEntryType_Frame.class);
		putCOHDType(9, COHDEntryType_List.class);
		putCOHDType(13, COHDEntryType_Scrollbar.class);
		putCOHDType(6, COHDEntryType_Slider.class);
	}

	/**
	 * The parsed ULDH Chunk
	 */
	public ULDH uldHeader;

	/**
	 * Parses the given ULD data pool
	 *
	 * @param data The data pool to use
	 * @throws IOException
	 */
	public ULD_File(final byte[] data, ByteOrder endian) throws IOException {
		super(endian);
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(endian);
		uldHeader = new ULDH(bb);
	}

	/**
	 * Adds a new Graphical node parser
	 *
	 * @param kind The type identifier to associate with the new handler
	 * @param nodeClass The node handler for the given identifier
	 */
	private static void putGraphicsType(int kind, Class<? extends GraphicsNodeTypeData> nodeClass) {
		graphicsTypes.put(kind, nodeClass);
	}

	/**
	 * Adds a new COHD Node parser
	 *
	 * @param kind The type identifier to associate with the new handler
	 * @param nodeClass The node handler for the given identifier
	 */
	private static void putCOHDType(int kind, Class<? extends COHDEntryType> nodeClass) {
		cohdTypes.put(kind, nodeClass);
	}

	/**
	 * Looks up a parser for the given type and returns a new instance with parsed data according to type.
	 * Available parsers should have previously been added by a call to putGraphicsType
	 *
	 * @param type The type identifier to find handler for
	 * @param data The data pool to use
	 * @return A new instance of parsed data for the given type
	 */
	public static GraphicsNodeTypeData getGraphicsNodeByType(int type, ByteBuffer data) {
		Class<? extends GraphicsNodeTypeData> aClass = graphicsTypes.get(type);
		if ( aClass != null ) {
			try {
				Constructor c = aClass.getDeclaredConstructor(ByteBuffer.class);
				return (GraphicsNodeTypeData)c.newInstance(data);
			} catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e ) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Looks up a parser for the given type and returns a new instance with parsed data according to type.
	 * Available parsers should have previously been added by a call to putCOHDType
	 *
	 * @param type The type identifier to find handler for
	 * @param data The data pool to use
	 * @return A new instance of parsed data for the given type
	 */
	public static COHDEntryType getCOHDNodeByType(int type, ByteBuffer data) {
		Class<? extends COHDEntryType> aClass = cohdTypes.get(type);
		if ( aClass != null ) {
			try {
				Constructor c = aClass.getDeclaredConstructor(ByteBuffer.class);
				return (COHDEntryType)c.newInstance(data);
			} catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e ) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Main routine for testing parsing
	 *
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		byte[] data = FileTools.getRaw("D:\\games\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv", "ui/uld/botanistgame.uld");
		/*File   f    = new File("D:\\svn\\ui\\uld\\botanistgame.uld");
		try {
			OutputStream os = new FileOutputStream(f);
			os.write(data);
		} catch ( IOException e ) {
			e.printStackTrace();
		}*/

		try {
			ULD_File uld = new ULD_File(data, ByteOrder.LITTLE_ENDIAN);
			System.out.println(uld);
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Convenience function for reading a fixed length string from a ByteBuffer
	 *
	 * @param buffer The buffer to read from
	 * @param byteLength The number of bytes to read
	 * @return A new non-trimmed string read from the given buffer with the given length.
	 */
	private static String getString(ByteBuffer buffer, int byteLength) {
		byte[] input = new byte[byteLength];
		buffer.get(input);
		return new String(input);
	}

	/**
	 * ULDH Chunk
	 */
	public static class ULDH {
		public ATKH[] atkhs = new ATKH[2];

		private int atkh0offset;
		private int atkh1offset;

		/**
		 * Initializes this ULDH Chunk from the given data pool
		 *
		 * @param data The data pool to use
		 */
		public ULDH(ByteBuffer data) {
			String sig = getString(data, 8);
			if ( sig.toLowerCase().equals("uldh0100") ) {
				atkh0offset = data.getInt();
				atkh1offset = data.getInt();
				if ( atkh0offset > 0 ) {
					data.position(atkh0offset);
					atkhs[0] = new ATKH(data);
				}
				if ( atkh1offset > 0 ) {
					data.position(atkh1offset);
					atkhs[1] = new ATKH(data);
				}
			} else {
				throw new RuntimeException("No ULDH Sginature");
			}
		}

		@Override
		public String toString() {
			return "ULDH{" +
				   "atkh0offset=" + atkh0offset +
				   ", atkh1offset=" + atkh1offset +
				   ", atkhs=" + Arrays.toString(atkhs) +
				   "}\n";
		}
	}

	/**
	 * ULD ATKH Chunk
	 */
	public static class ATKH {
		public ASHD ashd;
		public TPHD tphd;
		public COHD cohd;
		public TLHD tlhd;
		public WDHD wdhd;

		/**
		 * Initializes this ATKH by parsing the given data pool
		 *
		 * @param data The data pool to use
		 */
		public ATKH(ByteBuffer data) {
			int    atkhOffset = data.position();
			String signature  = getString(data, 8);
			if ( signature.toLowerCase().equals("atkh0100") ) {
				int ashdOffset = data.getInt() & 0xFFFF;
				int tphdOffset = data.getInt() & 0xFFFF;
				int cohdOffset = data.getInt() & 0xFFFF;
				int tlhdOffset = data.getInt() & 0xFFFF;
				int wdhdOffset = data.getInt() & 0xFFFF;
				if ( ashdOffset > 0 ) {
					data.position(atkhOffset + ashdOffset);
					ashd = new ASHD(data);
				}
				if ( tphdOffset > 0 ) {
					data.position(atkhOffset + tphdOffset);
					tphd = new TPHD(data);
				}
				if ( cohdOffset > 0 ) {
					data.position(atkhOffset + cohdOffset);
					cohd = new COHD(data);
				}
				if ( tlhdOffset > 0 ) {
					data.position(atkhOffset + tlhdOffset);
					tlhd = new TLHD(data);
				}
				if ( wdhdOffset > 0 ) {
					data.position(atkhOffset + wdhdOffset);
					wdhd = new WDHD(data);
				}
			}
			//String signature =
		}

		@Override
		public String toString() {
			return "ATKH{" +
				   "ashd=" + ( ashd != null ? ashd : "null" ) +
				   ", tphd=" + ( tphd != null ? tphd : "null" ) +
				   ", cohd=" + ( cohd != null ? cohd : "null" ) +
				   ", tlhd=" + ( tlhd != null ? tlhd : "null" ) +
				   ", wdhd=" + ( wdhd != null ? wdhd : "null" ) +
				   "}\n";
		}
	}

	/**
	 * ASHD Chunk
	 */
	public static class ASHD {
		public final SparseArray<String> paths = new SparseArray<>();

		/**
		 *
		 * @param data The data pool to use
		 */
		public ASHD(ByteBuffer data) {
			String signature = getString(data, 8);
			if ( signature.equals("ashd0100") ) {
				int count = data.getInt() & 0xFFFF;
				data.getInt();  //Align?
				for ( int i = 0; i < count; i++ ) {
					int index = data.getInt();
					String path = getString(data, 48).trim();
					paths.append(index, path);
				}
			}
		}
	}

	/**
	 * TPHD Chunk
	 */
	public static class TPHD {
		public final SparseArray<ImageSet> imageSets = new SparseArray<>();

		/**
		 * Initializes this TPHD Chunk from the given data pool
		 *
		 * @param data The data pool to use
		 */
		public TPHD(ByteBuffer data) {
			String signature = getString(data, 8);
			if ( signature.equals("tphd0100") ) {
				int count = data.getInt() & 0xFFFF;
				data.getInt(); //Align?
				for ( int i = 0; i < count; i++ ) {
					ImageSet set = new ImageSet(data);
					imageSets.append(set.index, set);
				}
			}
		}

		@Override
		public String toString() {
			return "TPHD{" +
				   "imageSets=" + imageSets +
				   "}\n";
		}
	}

	/**
	 * TLHD Chunk
	 */
	public static class TLHD {
		final SparseArray<TLHDSet> entries = new SparseArray<>();

		/**
		 * Initializes this TLHD Chunk from the given data pool
		 *
		 * @param data The data pool to use
		 */
		public TLHD(ByteBuffer data) {
			String signature = getString(data, 8);
			if ( signature.equals("tlhd0100") ) {
				int count = data.getInt() & 0xFFFF;
				data.getInt(); //Align?
				for ( int i = 0; i < count; i++ ) {
					TLHDSet entry = new TLHDSet(data);
					entries.put(entry.index, entry);
				}
			}
		}

		@Override
		public String toString() {
			return "TLHD{" +
				   "entries=" + entries +
				   "}\n";
		}
	}

	/**
	 * COHD Chunk
	 */
	public static class COHD {
		final SparseArray<COHDEntry> entries = new SparseArray<>();

		public SparseArray<COHDEntry> getEntries() {
			return entries;
		}

		/**
		 * Initializes this COHD Chunk from the given data pool
		 *
		 * @param data The data pool to use
		 */
		public COHD(ByteBuffer data) {
			String signature = getString(data, 8);
			if ( signature.equals("cohd0100") ) {
				int count = data.getInt() & 0xFFFF;
				data.getInt(); //Align?
				for ( int i = 0; i < count; i++ ) {
					COHDEntry entry = new COHDEntry(data);
					entries.put(entry.index, entry);
				}
			}
		}

		@Override
		public String toString() {
			return "COHD{" +
				   "entries=" + entries +
				   "}\n";
		}
	}

	/**
	 * WDHD Chunk
	 */
	public static class WDHD {
		final SparseArray<WDHDEntry> entries = new SparseArray<>();

		public SparseArray<WDHDEntry> getEntries() {
			return entries;
		}

		/**
		 * Initializes this WDHD Chunk from the given data pool
		 *
		 * @param data The data pool to use
		 */
		public WDHD(ByteBuffer data) {
			String signature = getString(data, 8);
			if ( signature.equals("wdhd0100") ) {
				int count = data.getInt() & 0xFFFF;
				data.getInt(); //Align?
				for ( int i = 0; i < count; i++ ) {
					WDHDEntry entry = new WDHDEntry(data);
					entries.put(entry.index, entry);
				}
			}
		}

		@Override
		public String toString() {
			return "WDHD{" +
				   "entries=" + entries +
				   '}';
		}
	}

	@Override
	public String toString() {
		return "ULD_File{" +
			   "uldHeader=" + ( uldHeader != null ? uldHeader : "null" ) +
			   "}\n";
	}
}
