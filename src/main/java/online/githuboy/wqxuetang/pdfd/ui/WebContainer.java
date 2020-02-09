package online.githuboy.wqxuetang.pdfd.ui;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import online.githuboy.wqxuetang.pdfd.App;
import online.githuboy.wqxuetang.pdfd.AppContext;

/**
 * WebContainer
 *
 * @author suchu
 * @since 2020年2月10日
 */
@Slf4j
public class WebContainer {
    final WebView browser;
    WebEngine webEngine;
    private Button button;
    @Getter
    private VBox stackPane;

    public WebContainer() {
        button = new Button("InvokeJsFunction");
        browser = new WebView();
        webEngine = browser.getEngine();
        stackPane = new VBox();
        stackPane.setSpacing(20);
        stackPane.setSpacing(20);
        stackPane.getChildren().addAll(/*button,*/ browser);
        initEngine();
        //  initEvent();
    }


    private void initEvent() {
        button.setOnAction(event -> {
            log.info("NVC:{}", getNVC());
        });
    }

    public String getNVC() {
        if (null == webEngine) {
            log.error("webEngine未初始化");
            return null;
        }
        try {
            JSObject win = (JSObject) webEngine.executeScript("window");
            Object eval = win.eval("getNvc()");
            System.out.println(eval);
            return (String) eval;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initEngine() {
        webEngine.load("http://localhost:8081/nvc");
        WebContainer ref = this;
        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> ov, Worker.State oldState,
                 Worker.State newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            String nvc = getNVC();
                            if (null == nvc) {
                                log.error("程序初始化失败");
                                System.exit(1);
                            } else {
                                AppContext.setWebContainer(ref);
                                Thread appThread = new Thread(() -> {
                                    App app = new App();
                                    app.start();
                                }, "app-thread");
                                appThread.start();
                            }
                        } catch (Exception e) {
                            log.error("程序启动失败：", e);
                        }

                    } else {
                        log.error("加载授权url失败:{}", newState);
//                        System.exit(1);
                    }
                });
    }
}
