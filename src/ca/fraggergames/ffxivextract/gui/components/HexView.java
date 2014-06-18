package ca.fraggergames.ffxivextract.gui.components;

import java.awt.GridBagConstraints;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class HexView extends JScrollPane{

	JTextArea txtHexData = new JTextArea();
	int columnCount;
	
	public HexView(int columnCount)
	{		
		this.columnCount = columnCount;
		
		//TextArea Setup
		txtHexData.setEditable(false);
		
		//Layout Setup
		//JScrollPane scrollPane = new JScrollPane();
		//scrollPane.getViewport().add(txtHexData);
		setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		//setLayout(new GridLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
		getViewport().add(txtHexData);		
	}
	
	public void setBytes(byte[] byteArray)
	{
		createTextAreaBody(byteArray, columnCount);
	}

	private void createTextAreaBody(byte[] byteArray, int columnCount) {
		String data = "";
		int valBeginofLastRow = 0;
		int total;
		if (byteArray.length < columnCount) 
			total = columnCount; 
		else
			total = byteArray.length + (byteArray.length % columnCount);  
		
		for (int i = 0; i < total; i++)
		{										
			//New Row
			if (i % columnCount == 0)
			{				
				//Ignore if just started
				if (i != 0)
				{
					//Do ASCII and new row
					for (int j = valBeginofLastRow; j < i; j++)
					{
						if (j > byteArray.length -1)
							data += 'x';
						else if (byteArray[j] < 32 || byteArray[j] > 126)
							data += '.';
						else
							data += (char) byteArray[j];
					}
					data += "\n";
				}				
				valBeginofLastRow = i;				
				data += String.format("0x%08X: ", i);
			}
			
			if (i > byteArray.length-1)
				data += "XX ";
			else
				data += String.format("%02X ", byteArray[i]);
			
		}
		
		//Left over stuff incase the mod isn't exact
				
			for (int i = valBeginofLastRow; i < byteArray.length + (byteArray.length % columnCount); i++)
			{
				if (i > byteArray.length -1)
					data += ' ';
				else if (byteArray[i] < 32 || byteArray[i] > 126)
					data += '.';
				else
					data += (char) byteArray[i];
			}
					
				
		txtHexData.setText(data);
		txtHexData.setCaretPosition(0);
	}
			
}
