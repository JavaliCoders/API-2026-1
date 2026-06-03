package api.util;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.HashMap;
import java.util.Map;

public final class LucideIconFactory {

    private static final Map<String, String[]> ICONS = createIcons();

    private LucideIconFactory() {
    }

    public static StackPane create(String key, String color, double size) {
        String[] paths = ICONS.getOrDefault(key, ICONS.get("file-text"));
        Group group = new Group();

        for (String content : paths) {
            SVGPath path = new SVGPath();
            path.setContent(content);
            path.setFill(Color.TRANSPARENT);
            path.setStroke(Color.web(color));
            path.setStrokeWidth(2);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            group.getChildren().add(path);
        }

        double scale = size / 24.0;
        group.setScaleX(scale);
        group.setScaleY(scale);

        StackPane wrapper = new StackPane(group);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setMouseTransparent(true);
        return wrapper;
    }

    public static boolean hasIcon(String key) {
        return ICONS.containsKey(key);
    }

    private static Map<String, String[]> createIcons() {
        Map<String, String[]> icons = new HashMap<>();
        icons.put("arrow-down-right", new String[]{
                "M7 7l10 10",
                "M17 7v10H7"
        });
        icons.put("arrow-up-right", new String[]{
                "M7 7h10v10",
                "M7 17 17 7"
        });
        icons.put("banknote", new String[]{
                "M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Z",
                "M10 12a2 2 0 1 0 4 0a2 2 0 1 0-4 0",
                "M6 12h.01",
                "M18 12h.01"
        });
        icons.put("bell", new String[]{
                "M10.268 21a2 2 0 0 0 3.464 0",
                "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326"
        });
        icons.put("boxes", new String[]{
                "M2.97 12.92A2 2 0 0 0 2 14.63v3.24a2 2 0 0 0 .97 1.71l3 1.8a2 2 0 0 0 2.06 0L12 19v-5.5l-5-3-4.03 2.42Z",
                "M7 16.5 2.26 13.65",
                "M7 16.5l5-3",
                "M7 16.5v5.17",
                "M12 13.5V19l3.97 2.38a2 2 0 0 0 2.06 0l3-1.8a2 2 0 0 0 .97-1.71v-3.24a2 2 0 0 0-.97-1.71L17 10.5l-5 3Z",
                "M17 16.5l-5-3",
                "M17 16.5l4.74-2.85",
                "M17 16.5v5.17",
                "M7.97 4.42A2 2 0 0 0 7 6.13v4.37l5 3 5-3V6.13a2 2 0 0 0-.97-1.71l-3-1.8a2 2 0 0 0-2.06 0l-3 1.8Z",
                "M12 8 7.26 5.15",
                "M12 8l4.74-2.85",
                "M12 13.5V8"
        });
        icons.put("building-2", new String[]{
                "M10 12h4",
                "M10 8h4",
                "M14 21v-3a2 2 0 0 0-4 0v3",
                "M6 10H4a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2",
                "M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16"
        });
        icons.put("building2", icons.get("building-2"));
        icons.put("calendar", new String[]{
                "M8 2v4",
                "M16 2v4",
                "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z",
                "M3 10h18"
        });
        icons.put("circle-check", new String[]{
                "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0 -20",
                "M9 12l2 2 4-4"
        });
        icons.put("circle-x", new String[]{
                "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0 -20",
                "M15 9l-6 6",
                "M9 9l6 6"
        });
        icons.put("clock", new String[]{
                "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0 -20",
                "M12 6v6l4 2"
        });
        icons.put("clock-4", icons.get("clock"));
        icons.put("download", new String[]{
                "M12 15V3",
                "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
                "M7 10l5 5 5-5"
        });
        icons.put("file-text", new String[]{
                "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2Z",
                "M14 2v5a1 1 0 0 0 1 1h5",
                "M10 9H8",
                "M16 13H8",
                "M16 17H8"
        });
        icons.put("layout-dashboard", new String[]{
                "M4 3h5a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z",
                "M15 3h5a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z",
                "M15 12h5a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1Z",
                "M4 16h5a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1Z"
        });
        icons.put("log-out", new String[]{
                "M16 17l5-5-5-5",
                "M21 12H9",
                "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"
        });
        icons.put("menu", new String[]{
                "M4 5h16",
                "M4 12h16",
                "M4 19h16"
        });
        icons.put("package", new String[]{
                "M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73Z",
                "M12 22V12",
                "M3.29 7 12 12 20.71 7",
                "M7.5 4.27l9 5.15"
        });
        icons.put("package-check", new String[]{
                "M16 16l2 2 4-4",
                "M21 10V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l2-1.14",
                "M7.5 4.27l9 5.15",
                "M3.29 7 12 12 20.71 7",
                "M12 22V12"
        });
        icons.put("package-minus", new String[]{
                "M16 16h6",
                "M21 10V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l2-1.14",
                "M7.5 4.27l9 5.15",
                "M3.29 7 12 12 20.71 7",
                "M12 22V12"
        });
        icons.put("shopping-cart", new String[]{
                "M8 20a1 1 0 1 0 0 2a1 1 0 1 0 0-2",
                "M19 20a1 1 0 1 0 0 2a1 1 0 1 0 0-2",
                "M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"
        });
        icons.put("timer", new String[]{
                "M10 2h4",
                "M12 14l3-3",
                "M12 6a8 8 0 1 0 0 16a8 8 0 1 0 0 -16"
        });
        icons.put("trending-up", new String[]{
                "M16 7h6v6",
                "M22 7l-8.5 8.5-5-5L2 17"
        });
        icons.put("triangle-alert", new String[]{
                "M21.73 18l-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",
                "M12 9v4",
                "M12 17h.01"
        });
        icons.put("users", new String[]{
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M16 3.128a4 4 0 0 1 0 7.744",
                "M22 21v-2a4 4 0 0 0-3-3.87",
                "M9 3a4 4 0 1 0 0 8a4 4 0 1 0 0 -8"
        });
        icons.put("x", new String[]{
                "M18 6 6 18",
                "M6 6l12 12"
        });
        return icons;
    }
}