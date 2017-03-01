package sophie.tools.textfile;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sophie.io.CharsetTeller;
import sophie.tools.textfile.sort.Configuration;
import sophie.tools.textfile.sort.Sort;
import sophie.widget.ErrorMessageDialog;
import sophie.widget.JFileTextField;

@SuppressWarnings("serial")
public class BagDiff extends JPanel {
	private static final String VERSION = "1.00";
	private static final String TITLE = BagDiff.class.getSimpleName() + " " + VERSION;
	private static final String AUTOMATIC = "Automatic";
	private static final String DEFAULT = "Default";
	private static final String NONE = "None";
	private JFileTextField newDocumentTextField;
	private JFileTextField oldDocumentTextField;
	private JFileTextField outDocumentTextField;
	private JComboBox<Object> newEncodingComboBox;
	private JComboBox<Object> oldEncodingComboBox;
	private JComboBox<Object> outEncodingComboBox;
	private JComboBox<Object> localeComboBox;
	private JCheckBox selectNewCheckBox;
	private JCheckBox selectOldCheckBox;
	private JCheckBox selectCommonCheckBox;
	private JTextField newPrefixTextField;
	private JTextField oldPrefixTextField;
	private JTextField commonPrefixTextField;
	private boolean selectNew = true;
	private boolean selectOld = true;
	private boolean selectCommon = true;
	private String prefixNew = "++";
	private String prefixOld = "--";
	private String prefixCommon = "==";

	void browseNewDocument() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Choose New Document");
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           newDocumentTextField.setText(chooser.getSelectedFile().getPath().replace('\\', '/'));
        }
	}
	
	void browseOldDocument() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Choose Old Document");
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           oldDocumentTextField.setText(chooser.getSelectedFile().getPath().replace('\\', '/'));
        }
	}
    
  	void browseOutDocumentFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Choose Out Document file");
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String name = chooser.getSelectedFile().getPath().replace('\\', '/');
            outDocumentTextField.setText(name);
        }
	}
  	
  	void diff(File newDocumentFile, Charset newCharset, File oldDocumentFile, Charset oldCharset, File outDocumentFile, Charset outCharset, Locale locale) throws IOException {
  		Collator collator = (locale != null)? Collator.getInstance(locale): null;
  		BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outDocumentFile), outCharset));
  		try {
  			BufferedReader newReader = new BufferedReader(new InputStreamReader(new FileInputStream(newDocumentFile),  newCharset));
  			try {
  				BufferedReader oldReader = new BufferedReader(new InputStreamReader(new FileInputStream(oldDocumentFile),  oldCharset));
  				try {
  					String newLine = newReader.readLine();
  					String oldLine = oldReader.readLine();
  					while(newLine != null && oldLine != null) {
  						int comp = (collator != null)? collator.compare(newLine, oldLine): newLine.compareTo(oldLine);
  						if(comp < 0) {
  							if(selectNew) {
  								outWriter.write(prefixNew);
  								outWriter.write(newLine);
  								outWriter.newLine();
  							}
  							newLine = newReader.readLine();
  						} else if(comp > 0) {
  							if(selectOld) {
  								outWriter.write(prefixOld);
  								outWriter.write(oldLine);
  								outWriter.newLine();
  							}
  							oldLine = oldReader.readLine();
  						} else {
  							if(selectCommon) {
  								outWriter.write(prefixCommon);
  								outWriter.write(newLine);
  								outWriter.newLine();
  							}
  							newLine = newReader.readLine();
  							oldLine = oldReader.readLine();
  						}
  					}
  					while(newLine != null) {
  						if(selectNew) {
  							outWriter.write(prefixNew);
  							outWriter.write(newLine);
  							outWriter.newLine();
  						}
  						newLine = newReader.readLine();
  					}
  					while(oldLine != null) {
  						if(selectOld) {
  							outWriter.write(prefixOld);
  							outWriter.write(oldLine);
  							outWriter.newLine();
  						}
  						oldLine = oldReader.readLine();
  					}
  				} finally {
  					oldReader.close();
  				}
  			} finally {
  				newReader.close();
  			}
  		} finally {
  			outWriter.close();
  		}
  	}
  	
  	static String unescape(String s) {
		StringBuilder builder = new StringBuilder();
		char[] ca = s.toCharArray();
		for(int i = 0; i < ca.length; i++) {
			char c = ca[i];
			if((c == '\\') && i + 1 <  ca.length) {
				i++;
				c = ca[i];
				switch(c) {
				case 't': builder.append('\t'); break;
				case 'r': builder.append('\r'); break;
				case 'n': builder.append('\n'); break;
				case 'f': builder.append('\f'); break;
				case '0': builder.append('\0'); break;
				default: builder.append(c);
				}
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
  	}

	void diff() {
        try {
            String newDocumentFileName = newDocumentTextField.getText().replace('\\', '/').trim();
            String oldDocumentFileName = oldDocumentTextField.getText().replace('\\', '/').trim();
            String outDocumentFileName = outDocumentTextField.getText().replace('\\', '/').trim();
            File newDocumentFile = new File(newDocumentFileName);
            File oldDocumentFile = new File(oldDocumentFileName);
            File outDocumentFile = new File(outDocumentFileName);
            if(!outDocumentFile.exists() || JOptionPane.showConfirmDialog(this, outDocumentFile.toString().replace('\\', '/') + " exists.  Overwrite?", TITLE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            	selectNew = selectNewCheckBox.isSelected();
            	selectOld = selectOldCheckBox.isSelected();
            	selectCommon = selectCommonCheckBox.isSelected();
            	prefixNew = unescape(newPrefixTextField.getText());
            	prefixOld = unescape(oldPrefixTextField.getText());
            	prefixCommon = unescape(commonPrefixTextField.getText());
    			final Charset newEncoding;
    			if(newEncodingComboBox.getSelectedItem() == AUTOMATIC) {
    				newEncoding = CharsetTeller.getCharset(newDocumentFile);
    			} else if(newEncodingComboBox.getSelectedItem() == DEFAULT) {
    				newEncoding = Charset.defaultCharset();
    			} else {
    				newEncoding = (Charset) newEncodingComboBox.getSelectedItem();
    			}
    			final Charset oldEncoding;
    			if(oldEncodingComboBox.getSelectedItem() == AUTOMATIC) {
    				oldEncoding = CharsetTeller.getCharset(oldDocumentFile);
    			} else if(oldEncodingComboBox.getSelectedItem() == DEFAULT) {
    				oldEncoding = Charset.defaultCharset();
    			} else {
    				oldEncoding = (Charset) oldEncodingComboBox.getSelectedItem();
    			}
    			final Charset outEncoding;
    			if(outEncodingComboBox.getSelectedItem() == AUTOMATIC) {
    				outEncoding = newEncoding;
    			} else if(outEncodingComboBox.getSelectedItem() == DEFAULT) {
    				outEncoding = Charset.defaultCharset();
    			} else {
    				outEncoding = (Charset) outEncodingComboBox.getSelectedItem();
    			}
    			final Locale locale;
    			if(localeComboBox.getSelectedItem() == NONE) {
    				locale = null;
    			} else if(localeComboBox.getSelectedItem() == DEFAULT) {
    				locale = Locale.getDefault();
    			} else {
    				locale = (Locale)localeComboBox.getSelectedItem();
    			}
    			File tempNewFile = File.createTempFile("BagDiff", ".new");
    			try {
        			File tempOldFile = File.createTempFile("BagDiff", ".old");
        			try {
            			Configuration configuration = new Configuration();
            			configuration.setInputFileNames(new String[] {newDocumentFile.getAbsolutePath()});
            			configuration.setOutputFileName(tempNewFile.getAbsolutePath());
            			configuration.setInputEncoding(newEncoding);
            			configuration.setOutputEncoding(newEncoding);
            			configuration.setTextLocale(locale);
            			Sort.sort(configuration);
            			configuration = new Configuration();
            			configuration.setInputFileNames(new String[] {oldDocumentFile.getAbsolutePath()});
            			configuration.setOutputFileName(tempOldFile.getAbsolutePath());
            			configuration.setInputEncoding(oldEncoding);
            			configuration.setOutputEncoding(oldEncoding);
            			configuration.setTextLocale(locale);
            			Sort.sort(configuration);
            	        diff(tempNewFile, newEncoding, tempOldFile, oldEncoding, outDocumentFile, outEncoding, locale);
        			} finally {
        				tempOldFile.delete();
        			}
    			} finally {
    				tempNewFile.delete();
    			}
            }
        } catch(Throwable e) {
        	ErrorMessageDialog.showMessageDialog(this, e, TITLE);
        }
	}
	
    Locale[] availableLocales() {
    	Locale[] locales = Locale.getAvailableLocales();
    	Arrays.sort(locales, new Comparator<Locale>() {
    		Collator collator = Collator.getInstance(Locale.US);

			@Override
			public int compare(Locale thisLocale, Locale thatLocale) {
				return collator.compare(thisLocale.toString(), thatLocale.toString());
			}});
    	return locales;
    }
    
    public BagDiff() {
        setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
		JButton newDocumentButton = new JButton("New Document");
        northPanel.add(newDocumentButton, gbc);
		gbc.gridx++; gbc.gridwidth = 4; 
		newDocumentTextField = new JFileTextField(40);
		northPanel.add(newDocumentTextField, gbc);
        //
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1;
		JButton oldDocumentButton = new JButton("Old Document");
        northPanel.add(oldDocumentButton, gbc);
		gbc.gridx++; gbc.gridwidth = 4;  gbc.weightx = 1.0;
		oldDocumentTextField = new JFileTextField(40);
		northPanel.add(oldDocumentTextField, gbc);
		//
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc. weightx = 0;
		JButton outDocumentFileButton = new JButton("Out Document");
		northPanel.add(outDocumentFileButton, gbc);
		gbc.gridx++; gbc.gridwidth = 4;
		outDocumentTextField = new JFileTextField(40);
		northPanel.add(outDocumentTextField, gbc);
		//
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1;
        northPanel.add(new JLabel("Encoding", JLabel.RIGHT), gbc);
        gbc.gridx++; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        ArrayList<Object> charsetList = new ArrayList<Object>();
        charsetList.add(AUTOMATIC);
        charsetList.add(DEFAULT);
        for(Charset charset: Charset.availableCharsets().values()) {
        	charsetList.add(charset);
        }
        JPanel encodingPanel = new JPanel();
        encodingPanel.add(new JLabel("New"));
        newEncodingComboBox = new JComboBox<Object>(charsetList.toArray());
        encodingPanel.add(newEncodingComboBox);
        encodingPanel.add(new JLabel("Old"));
        oldEncodingComboBox = new JComboBox<Object>(charsetList.toArray());
        encodingPanel.add(oldEncodingComboBox);
        encodingPanel.add(new JLabel("Out"));
        outEncodingComboBox = new JComboBox<Object>(charsetList.toArray());
        encodingPanel.add(outEncodingComboBox);
        northPanel.add(encodingPanel, gbc);
        //
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        northPanel.add(new JLabel("Locale"), gbc);
        gbc.gridx++; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        ArrayList<Object> textLocaleList = new ArrayList<Object>();
        textLocaleList.add(DEFAULT);
        textLocaleList.add(NONE);
        Locale[] availableLocales = availableLocales();
        for(Locale locale: availableLocales) {
        	textLocaleList.add(locale);
        }
        localeComboBox = new JComboBox<Object>(textLocaleList.toArray());
        northPanel.add(localeComboBox, gbc);
		//
		gbc.weightx = 0;
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		northPanel.add(new JLabel("Select"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		selectNewCheckBox = new JCheckBox("New", selectNew);
		northPanel.add(selectNewCheckBox, gbc);
		gbc.gridx++;
		selectOldCheckBox = new JCheckBox("Old", selectOld);
		northPanel.add(selectOldCheckBox, gbc);
		gbc.gridx++;
		selectCommonCheckBox = new JCheckBox("Common", selectCommon);
		northPanel.add(selectCommonCheckBox, gbc);
		//
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		northPanel.add(new JLabel("Prefixes"), gbc);
		gbc.gridx++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		JPanel innerPanel = new JPanel();
		innerPanel.add(new JLabel("New"));
		newPrefixTextField = new JTextField(prefixNew, 2);
		innerPanel.add(newPrefixTextField);
		northPanel.add(innerPanel, gbc);
		gbc.gridx++;
		innerPanel = new JPanel();
		innerPanel.add(new JLabel("Old"));
		oldPrefixTextField = new JTextField(prefixOld, 2);
		innerPanel.add(oldPrefixTextField);
		northPanel.add(innerPanel, gbc);
		gbc.gridx++;
		innerPanel = new JPanel();
		innerPanel.add(new JLabel("Common"));
		commonPrefixTextField = new JTextField(prefixCommon, 2);
		innerPanel.add(commonPrefixTextField);
		northPanel.add(innerPanel, gbc);
		gbc.gridx++;
		northPanel.add(new JLabel("(\\t for tab code)"), gbc);
        //
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(2, 2, 8, 2);
		JButton diffButton = new JButton("Diff");
		northPanel.add(diffButton, gbc);
		add(northPanel, BorderLayout.NORTH);

		newDocumentButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        browseNewDocument();
		    }
		});

		oldDocumentButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        browseOldDocument();
		    }
		});

		outDocumentFileButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        browseOutDocumentFile();
		    }
		});
        diffButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        diff();
		    }
		});
    }
    
	public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().add(new BagDiff(), BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle(TITLE);
        f.pack();
        f.setVisible(true);
    }
}
