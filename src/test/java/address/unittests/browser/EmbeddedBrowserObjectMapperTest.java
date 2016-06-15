package address.unittests.browser;

import address.browser.embeddedbrowser.EbEditorCommand;
import address.browser.jxbrowser.EmbeddedBrowserObjectMapper;
import com.teamdev.jxbrowser.chromium.EditorCommand;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Tests the EmbeddedBrowserObjectMapperTest
 */
public class EmbeddedBrowserObjectMapperTest {

    @Test
    public void testConvertEbEditorCommand_mapScrollToTheEnd_validObject(){
        assertTrue(EmbeddedBrowserObjectMapper.convertEbEditorCommand(EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT)
                   == EditorCommand.SCROLL_TO_END_OF_DOCUMENT);
    }

    @Test
    public void testConvertEbEditorCommand_mapInvalidChoice_exceptionThrown(){
        try {
            EditorCommand obj = EmbeddedBrowserObjectMapper.convertEbEditorCommand(10);
        } catch(IllegalArgumentException e) {
            return;
        }
        fail();
    }

}
