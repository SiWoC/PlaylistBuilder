package nl.siwoc.playlistBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class RelativeFilenamesModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7760680729953326441L;

	private static final Predicate<? super String> startsWithHash = new Predicate<String>() {
	    @Override
	    public boolean test(String t) {
	        return t.startsWith("#");
	    }
	};
	private static final Predicate<? super String> empty = new Predicate<String>() {
	    @Override
	    public boolean test(String t) {
	        return (t == null || t.trim().isEmpty());
	    }
	};

    private String[] columnNames = {"Relative filename"};
    private ArrayList<String> lines = new ArrayList<String>();
    private boolean modified = false;
    
    public boolean isModified() {
		return modified;
	}

	public static boolean checkEncoding(String s) {
        for(int i = 0;i < s.length();i++) {
            String test = "" + s.charAt(i);
            byte[] bytes;
			try {
				bytes = test.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				return false;
			}
            if (bytes.length > 1) {
                return false;
            }
        }
		return true;
	}

	public static void main(String[] args) throws IOException {
		String line = "\\\\NAS\\Music\\iets.mp3";
		System.out.println("1 " + line);
		line = line.replaceFirst("\\\\\\\\NAS\\\\Music", "..");
		System.out.println("2 " + line);
		RelativeFilenamesModel model = new RelativeFilenamesModel();
		model.readFile(new File("M:/00 playlists/Daphne.m3u"));
    }
    
	public int readFile(File file) throws IOException {
		if (file.isFile()) {
			lines.clear();
			String s = file.getAbsolutePath();
			ArrayList<String> readLines = (ArrayList<String>) Files.readAllLines(Paths.get(file.getAbsolutePath()));
			readLines.removeIf(startsWithHash);
			readLines.removeIf(empty);
			for (String line : readLines) {
				try {
					line = line.replaceFirst("\\\\\\\\NAS\\\\Music", "..");
					addRow(line);
				} catch (InvalidPathException e) {
					JOptionPane.showMessageDialog(null, "Bestand kan niet toegevoegd worden. " + e.getMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
				}
			}
			modified = false;
		    fireTableDataChanged();
		}
	    return getRowCount();
	}
	
	public int writeToFile(File file) throws IOException {
		int write = JOptionPane.YES_OPTION;
		if (file.isFile()) {
			write = JOptionPane.showConfirmDialog (null, "Weet je zeker dat je playlist " + file.getAbsolutePath() + " wilt overschrijven?", "Question", JOptionPane.YES_NO_OPTION);
		}
		if (write == JOptionPane.YES_OPTION) {
			System.out.println("lines to write: " + lines.size());
			Files.write(file.toPath(), lines);
			modified = false;
		}
		return write;
	}

	@Override
    public String getColumnName(int col) {
    	return columnNames[col];
    }
    
    @Override
	public int getColumnCount() {
        return columnNames.length;
	}

	@Override
	public int getRowCount() {
        return lines.size();
	}

	@Override
    public Object getValueAt(int row, int col) {
        return lines.get(row);
	}

	@Override
    public Class<String> getColumnClass(int c) {
        return String.class;
    }
	
	public int removeRow(int row) {
	    lines.remove(row);
	    modified = true;
	    fireTableRowsDeleted(row, row);
	    return getRowCount();
	}

	public int addRow(String filename) throws InvalidPathException{
		if (!checkEncoding(filename)) {
			throw new InvalidPathException(filename, "Filename bevat vreemde karakters");
		}
		if (!lines.contains(filename)) {
			lines.add(filename);
		    modified = true;
			fireTableRowsInserted(getRowCount() - 1, getRowCount());
		}
	    return getRowCount();
	}

	public int clear() {
		lines.clear();
		modified = false;
	    fireTableDataChanged();
	    return getRowCount();
	}
	
}
