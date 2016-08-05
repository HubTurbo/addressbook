package address.keybindings;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.hotkey.GlobalHotkeyEvent;
import address.testutil.BaseEventSubscriber;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.Provider;
import javafx.scene.input.KeyCodeCombination;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class GlobalHotkeyProviderTest {

    EventManager eventManager = EventManager.getInstance();
    GlobalHotkeyProvider globalHotkeyProvider;
    Provider provider;

    @Before
    public void setup(){
        globalHotkeyProvider = new GlobalHotkeyProvider(eventManager, LoggerManager.getLogger(KeyBindingsManager.class));
        provider = globalHotkeyProvider.provider;
    }

    @Test
    public void handleGlobalHotkeyEvent() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        eventManager.registerHandler(new BaseEventSubscriber() {
            @Subscribe
            @Override
            public void receive(BaseEvent e) {
                latch.countDown();
            }
        });

        globalHotkeyProvider.handleGlobalHotkeyEvent(new GlobalHotkeyEvent(KeyCodeCombination.valueOf("SHIFT + A")));

        //verify an event was posted
        assertTrue(latch.await(10, TimeUnit.SECONDS));

    }

    @After
    public void after() {
        EventManager.clearSubscribers();
    }

}