package net.md_5.bungee.api.chat;

import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.function.Function;

import static net.md_5.bungee.api.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentsLegacyTest {
    @Test
    public void testToLegacyFromLegacy()
    {
        String text = "" + GREEN + BOLD + "Hello " + WHITE + MAGIC + "world" + GRAY + "!";
        assertEquals( text, BaseComponent.toLegacyText( TextComponent.fromLegacyText( text ) ) );
    }
    @Test
    public void testLegacyComponentBuilderAppend()
    {
        String text = "" + GREEN + BOLD + "Hello " + RESET + MAGIC + "world" + GRAY + "!";
        BaseComponent[] components = TextComponent.fromLegacyText( text );
        BaseComponent[] builderComponents = new ComponentBuilder().append( components ).create();
        assertArrayEquals( components, builderComponents );
    }

    @Test
    public void testBuilderAppendLegacyCreate()
    {
        testBuilderAppendLegacy(
                ComponentBuilder::create,
                BaseComponent::toPlainText,
                YELLOW + "Hello " + GREEN + "world!",
                BaseComponent::toLegacyText
        );
    }

    @Test
    public void testLegacyConverter()
    {
        BaseComponent[] test1 = TextComponent.fromLegacyText( AQUA + "Aqua " + RED + BOLD + "RedBold" );

        assertEquals( "Aqua RedBold", BaseComponent.toPlainText( test1 ) );
        assertEquals( AQUA + "Aqua " + RED + BOLD + "RedBold", BaseComponent.toLegacyText( test1 ) );

        BaseComponent[] test2 = TextComponent.fromLegacyText( "Text http://spigotmc.org " + GREEN + "google.com/test" );

        assertEquals( "Text http://spigotmc.org google.com/test", BaseComponent.toPlainText( test2 ) );
        //The extra ChatColor instances are sometimes inserted when not needed but it doesn't change the result
        assertEquals( WHITE + "Text " + WHITE + "http://spigotmc.org" + WHITE
                + " " + GREEN + "google.com/test" + GREEN, BaseComponent.toLegacyText( test2 ) );

        ClickEvent url1 = test2[1].getClickEvent();
        assertNotNull( url1 );
        assertTrue( url1.getAction() == ClickEvent.Action.OPEN_URL );
        assertEquals( "http://spigotmc.org", url1.getValue() );

        ClickEvent url2 = test2[3].getClickEvent();
        assertNotNull( url2 );
        assertTrue( url2.getAction() == ClickEvent.Action.OPEN_URL );
        assertEquals( "http://google.com/test", url2.getValue() );
    }

    @Test
    public void testBuilderAppendLegacyBuild()
    {
        testBuilderAppendLegacy(
                ComponentBuilder::build,
                (component) -> BaseComponent.toPlainText( component ),
                // An extra format code is appended to the beginning because there is an empty TextComponent at the start of every component
                WHITE.toString() + YELLOW + "Hello " + GREEN + "world!",
                (component) -> BaseComponent.toLegacyText( component )
        );
    }

    private static <T> void testBuilderAppendLegacy(Function<ComponentBuilder, T> componentBuilder, Function<T, String> toPlainTextFunction, String expectedLegacyString, Function<T, String> toLegacyTextFunction)
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( YELLOW );
        builder.appendLegacy( GREEN + "world!" );

        T component = componentBuilder.apply( builder );

        assertEquals( "Hello world!", toPlainTextFunction.apply( component ) );
        assertEquals( expectedLegacyString, toLegacyTextFunction.apply( component ) );
    }


    @Test
    public void testLegacyResetInBuilderCreate()
    {
        testLegacyResetInBuilder(
                ComponentBuilder::create,
                ComponentSerializer::toString
        );
    }

    @Test
    public void testLegacyHack()
    {
        BaseComponent[] hexColored = new ComponentBuilder().color( of( Color.GRAY ) ).append( "Test" ).create();
        String legacy = BaseComponent.toLegacyText( hexColored );

        BaseComponent[] reColored = TextComponent.fromLegacyText( legacy );

        assertArrayEquals( hexColored, reColored );
    }

    @Test
    public void testInvalidColorCodes()
    {
        StringBuilder allInvalidColorCodes = new StringBuilder();

        // collect all invalid color codes (e.g. §z, §g, ...)
        for ( char alphChar : "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray() )
        {
            if ( ALL_CODES.indexOf( alphChar ) == -1 )
            {
                allInvalidColorCodes.append( COLOR_CHAR );
                allInvalidColorCodes.append( alphChar );
            }
        }

        // last char is a single '§'
        allInvalidColorCodes.append( COLOR_CHAR );

        String invalidColorCodesLegacyText = fromAndToLegacyText( allInvalidColorCodes.toString() );
        String emptyLegacyText = fromAndToLegacyText( "" );

        // all invalid color codes and the trailing '§' should be ignored
        assertEquals( emptyLegacyText, invalidColorCodesLegacyText );
    }

    private static String fromAndToLegacyText(String legacyText)
    {
        return BaseComponent.toLegacyText( TextComponent.fromLegacyText( legacyText ) );
    }

    @Test
    public void testLegacyResetInBuilderBuild()
    {
        testLegacyResetInBuilder(
                ComponentBuilder::build,
                ComponentSerializer::toString
        );
    }

    /*
     * In legacy chat, colors and reset both reset all formatting.
     * Make sure it works in combination with ComponentBuilder.
     */
    private static <T> void testLegacyResetInBuilder(Function<ComponentBuilder, T> componentBuilder, Function<T, String> componentSerializer)
    {
        ComponentBuilder builder = new ComponentBuilder();
        BaseComponent[] a = TextComponent.fromLegacyText( "" + DARK_RED + UNDERLINE + "44444" + RESET + "dd" + GOLD + BOLD + "6666" );

        String expected = "{\"extra\":[{\"underlined\":true,\"color\":\"dark_red\",\"text\":\"44444\"},{\"color\":"
                + "\"white\",\"text\":\"dd\"},{\"bold\":true,\"color\":\"gold\",\"text\":\"6666\"}],\"text\":\"\"}";
        assertEquals( expected, ComponentSerializer.toString( a ) );

        builder.append( a );

        String test1 = componentSerializer.apply( componentBuilder.apply( builder ) );
        assertEquals( expected, test1 );

        BaseComponent[] b = TextComponent.fromLegacyText( RESET + "rrrr" );
        builder.append( b );

        String test2 = componentSerializer.apply( componentBuilder.apply( builder ) );
        assertEquals(
                "{\"extra\":[{\"underlined\":true,\"color\":\"dark_red\",\"text\":\"44444\"},"
                        + "{\"color\":\"white\",\"text\":\"dd\"},{\"bold\":true,\"color\":\"gold\",\"text\":\"6666\"},"
                        + "{\"color\":\"white\",\"text\":\"rrrr\"}],\"text\":\"\"}",
                test2 );
    }
}
