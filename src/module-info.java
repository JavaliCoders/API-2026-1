/**
 * 
 */
/**
 * 
 */
module front_login {
	requires javafx.fxml;
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires javafx.base;

	exports api.model;

	opens api.controller to javafx.fxml;
	opens api.model to javafx.base, javafx.graphics, javafx.fxml;
}