package us.suselinks.quickquest;

import java.io.File;
import java.sql.Timestamp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.apache.commons.lang3.RandomStringUtils;

import us.suselinks.quickquest.fsobject.ViewableFSObject;

public class JavaFXMain extends Application {
	public static final String AppName = "QuickQuest";
	ObservableList<ViewableFSObject> matchedFilesList = FXCollections.observableArrayList();;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle(AppName);
		final File quickQuestIcon = new File(QuickQuest.QUICK_QUEST_PROG_DIR, "quickquest-icon-128x128.png");
		Image applicationIcon = null;
		if (quickQuestIcon.isFile()) {
			applicationIcon = new Image(quickQuestIcon.getAbsolutePath(), false);

		} else {
			// http://docs.oracle.com/javase/8/javafx/api/toc.htm
			// The image is located in us.suselinks.quickquest package of the
			// classpath
			applicationIcon = new Image("us/suselinks/quickquest/quickquest-icon-2.png", false);
		}
		primaryStage.getIcons().add(applicationIcon);
		StackPane root = new StackPane();
		final Menu fileMenu = new Menu("File");
		MenuItem exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
		exitMenuItem.setOnAction(e -> Platform.exit());

		fileMenu.getItems().add(exitMenuItem);
		final Menu editMenu = new Menu("Edit");
		// why accelerator does not work on Menu ?
		// editMenu.setAccelerator(new KeyCodeCombination(KeyCode.E,
		// KeyCombination.ALT_DOWN));
		MenuItem prefsMenuItem = new MenuItem("Preferences");
		editMenu.getItems().add(prefsMenuItem);

		final Menu helpMenu = new Menu("Help");
		MenuItem aboutMenuItem = new MenuItem("About");
		aboutMenuItem.setOnAction(e -> {
			matchedFilesList.get(0).setName(RandomStringUtils.randomNumeric(9));
			matchedFilesList.get(0).setPath(System.getProperty("user.dir"));
			matchedFilesList.get(0).setSize(RandomStringUtils.randomNumeric(9));
			matchedFilesList.get(0).setLtms(new Timestamp(System.currentTimeMillis()));
		});
		helpMenu.getItems().add(aboutMenuItem);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);

		TextField questTextField = new TextField();
		TableView<ViewableFSObject> matchedFilesTableView = new TableView<ViewableFSObject>();

		matchedFilesList.add(new ViewableFSObject());
		matchedFilesList.add(new ViewableFSObject());
		matchedFilesList.add(new ViewableFSObject());
		matchedFilesList.add(new ViewableFSObject());
		matchedFilesTableView.setItems(matchedFilesList);

		TableColumn<ViewableFSObject, String> nameCol = new TableColumn<ViewableFSObject, String>("Name");
		nameCol.setPrefWidth(200);
		nameCol.setCellValueFactory(new PropertyValueFactory("name"));

		TableColumn<ViewableFSObject, String> pathCol = new TableColumn<ViewableFSObject, String>("Path");
		pathCol.setPrefWidth(300);
		pathCol.setCellValueFactory(new PropertyValueFactory("path"));

		TableColumn<ViewableFSObject, String> sizeCol = new TableColumn<ViewableFSObject, String>("Size");
		sizeCol.setPrefWidth(100);
		sizeCol.setCellValueFactory(new PropertyValueFactory("size"));

		TableColumn<ViewableFSObject, Timestamp> lmtsCol = new TableColumn<ViewableFSObject, Timestamp>("Last Modified");
		lmtsCol.setPrefWidth(198);
		lmtsCol.setCellValueFactory(new PropertyValueFactory<ViewableFSObject, Timestamp>("lmts"));
		matchedFilesTableView.getColumns().setAll(nameCol, pathCol, sizeCol, lmtsCol);

		// WebView webView = new WebView();
		// WebEngine webEngine = webView.getEngine();
		// webEngine.load("http://suselinks.us");

		VBox vbox = new VBox();
		vbox.getChildren().addAll(menuBar, questTextField, matchedFilesTableView);
		root.getChildren().add(vbox);

		primaryStage.setScene(new Scene(root, 800, 600));
		primaryStage.show();
	}
}
