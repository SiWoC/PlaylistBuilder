package nl.siwoc.playlistBuilder;

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Toolkit;

public class MainWindow {

	private static final String M_PLAYLISTS = "M:/00 Playlists";
	private static final String NEW = "New";
	private static final String OPEN = "Open...";
	private static final String SAVE = "Save...";
	private JFrame frmPlaylistBuilder;
	private JTextField txtFilename;
	private JTable table;
	private JLabel lblSize = new JLabel("0");
	private RelativeFilenamesModel model = new RelativeFilenamesModel();
	private JFileChooser fileChooser;
	private File currentFile = null;

	public void setCurrentFile(File currentFileIn) {
		currentFile = currentFileIn;
		txtFilename.setText(currentFile.getPath());
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmPlaylistBuilder.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPlaylistBuilder = new JFrame();
		//frmPlaylistBuilder.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/resources/playlist.ico")));
		frmPlaylistBuilder.setIconImage(new ImageIcon(getClass().getResource("/resources/playlist.ico")).getImage());
		frmPlaylistBuilder.setTitle("Playlist Builder");
		frmPlaylistBuilder.setBounds(100, 100, 800, 600);
		frmPlaylistBuilder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPlaylistBuilder.setDropTarget(new DropTarget() {
			public synchronized void drop(DropTargetDropEvent evt) {
				evt.acceptDrop(DnDConstants.ACTION_COPY);
				try {
					addDroppedFiles((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Bestand kan niet toegevoegd worden. " + e.getLocalizedMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
				}
			}

			private void addDroppedFiles(List<File> droppedFiles) {
				Path pathBase = currentFile.getParentFile().toPath();
				for (File file : droppedFiles) {
					Path pathRelative = pathBase.relativize(file.toPath());
					try {
						lblSize.setText(String.valueOf(model.addRow(pathRelative.toString())));
					} catch (InvalidPathException e) {
						// showing the MessageBox here will continue the loop, throwing the exception will stop it.
						JOptionPane.showMessageDialog(null, "Bestand kan niet toegevoegd worden. " + e.getLocalizedMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		});

		JLabel lblFilename = new JLabel("Filename");

		JButton btnSave = new JButton("Save as...");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fileActionPerformed(SAVE);
			}
		});

		JButton btnOpen = new JButton(OPEN);
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (checkModifiedSave() == JOptionPane.YES_OPTION) {
					fileActionPerformed(OPEN);
				}
			}
		});

		txtFilename = new JTextField();
		txtFilename.setEditable(false);
		txtFilename.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		
		JButton btnNew = new JButton(NEW);
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (checkModifiedSave() == JOptionPane.YES_OPTION) {
					setCurrentFile(generateNewPlaylistFile());
					lblSize.setText(String.valueOf(model.clear()));
				}
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(frmPlaylistBuilder.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblFilename)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtFilename, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
							.addGap(18)
							.addComponent(lblSize, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNew, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnOpen, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSave, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFilename)
						.addComponent(btnSave)
						.addComponent(btnOpen)
						.addComponent(txtFilename, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNew)
						.addComponent(lblSize))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
					.addContainerGap())
		);

		table = new JTable();
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Right-click to delete a line
				if (e.getButton() == 3) {
					int row = table.rowAtPoint(e.getPoint());
					if (row > -1) {
						int dialogResult = JOptionPane.showConfirmDialog (null, "Weet je zeker dat je song " + model.getValueAt(row, 0) + " wilt verwijderen?", "Verwijderen?", JOptionPane.YES_NO_OPTION);
						if(dialogResult == JOptionPane.YES_OPTION){
							lblSize.setText(String.valueOf(model.removeRow(row)));
						}
					}
				}
			}
		});

		frmPlaylistBuilder.getContentPane().setLayout(groupLayout);
		
		table.setToolTipText("Klik met de rechter-muis-knop om een song te verwijderen");
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setModel(model);
		
		setCurrentFile(generateNewPlaylistFile());
	}

	private File generateNewPlaylistFile() {
		int i = 1;
		File newFile = new File(String.format(M_PLAYLISTS + "/newplaylist_%1$d.m3u", i));
		while (newFile.isFile()) {
			i++;
			newFile = new File(String.format(M_PLAYLISTS + "/newplaylist_%1$d.m3u", i));
		}
		return newFile;
	}

	private int fileActionPerformed(String action) {
		int fileChooserAction;
		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Playlists (*.m3u)", "m3u"));
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setSelectedFile(currentFile);

		if (OPEN.equals(action)) {
			fileChooser.setDialogTitle("Selecteer een playlist om te openen.");
			fileChooserAction = fileChooser.showOpenDialog(null);
			if (fileChooserAction == JFileChooser.APPROVE_OPTION) {
				try {
					lblSize.setText(String.valueOf(model.readFile(fileChooser.getSelectedFile())));
					setCurrentFile(fileChooser.getSelectedFile());
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Bestand kan niet geopend worden. " + e.getLocalizedMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
					return JOptionPane.NO_OPTION;
				}
			}
		} else { // SAVE
			fileChooser.setDialogTitle("Selecteer waar de playlist moet worden opgeslagen.");
			if (currentFile.isFile()) {
				fileChooser.setSelectedFile(currentFile);
			}
			fileChooserAction = fileChooser.showSaveDialog(null);
			if (fileChooserAction == JFileChooser.APPROVE_OPTION) {
				try {
					int writeOK = model.writeToFile(fileChooser.getSelectedFile());
					if (writeOK == JOptionPane.YES_OPTION) {
						setCurrentFile(fileChooser.getSelectedFile());
					}
					return writeOK;
				} catch (IOException e) {
					e.printStackTrace();
					if (SAVE.equals(action)) {
						JOptionPane.showMessageDialog(null, "Bestand kan niet opgeslagen worden. " + e.getLocalizedMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
						return JOptionPane.NO_OPTION;
					} else {
						return JOptionPane.showConfirmDialog(null, "Bestand kan niet opgeslagen worden. " + e.getLocalizedMessage() + " Wil je doorgaan?", "Fout", JOptionPane.YES_NO_OPTION);
					}
				}
			}
		}
		return JOptionPane.YES_OPTION;
	}

	private int checkModifiedSave() {
		if (model.isModified()) {
			//do you want to save
			int save = JOptionPane.showConfirmDialog(null, "De playlist is gewijzigd. Wil je hem opslaan?", "Opslaan?", JOptionPane.YES_NO_CANCEL_OPTION);
			switch (save) {
				case JOptionPane.YES_OPTION: 
					return fileActionPerformed(SAVE);
				case JOptionPane.CANCEL_OPTION:
					return JOptionPane.NO_OPTION;
			}
		}
		return JOptionPane.YES_OPTION;
	}
}
