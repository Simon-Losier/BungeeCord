package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.md_5.bungee.api.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;

public class ComponentsFormatTest {
    @Test
    public void testHasFormatting()
    {
        BaseComponent component = new TextComponent();
        assertFalse( component.hasFormatting() );

        component.setBold( true );
        assertTrue( component.hasFormatting() );
    }

    @Test
    public void testFormatNotColor()
    {
        BaseComponent[] component = new ComponentBuilder().color( BOLD ).append( "Test" ).create();

        String json = ComponentSerializer.toString( component );
        BaseComponent[] parsed = ComponentSerializer.parse( json );

        assertNull( parsed[0].getColorRaw(), "Format should not be preserved as color" );
    }

    @Test
    public void testFormattingOnlyTextConversion()
    {
        String text = "" + GREEN;

        BaseComponent[] converted = TextComponent.fromLegacyText( text );
        assertEquals( GREEN, converted[0].getColor() );

        String roundtripLegacyText = BaseComponent.toLegacyText( converted );

        // color code should not be lost during conversion
        assertEquals( text, roundtripLegacyText );
    }

    @Test
    public void testFormatRetentionCopyFormattingCreate()
    {
        testFormatRetentionCopyFormatting( () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Test" ).create() ) );
    }

    @Test
    public void testFormatRetentionCopyFormattingBuild()
    {
        testFormatRetentionCopyFormatting( () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new ComponentBuilder( "Test" ).build() ) ) );
    }
    @Test
    public void testBuilderCreateFormatRetention()
    {
        testBuilderFormatRetention(
                ComponentBuilder::create,
                (components, index) -> components[index]
        );
    }

    @Test
    public void testBuilderBuildFormatRetention()
    {
        testBuilderFormatRetention(
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index )
        );
    }


    private static void testFormatRetentionCopyFormatting(Supplier<HoverEvent> hoverEventSupplier)
    {
        TextComponent first = new TextComponent( "Hello" );
        first.setBold( true );
        first.setColor( RED );
        first.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "test" ) );
        first.setHoverEvent( hoverEventSupplier.get() );

        TextComponent second = new TextComponent( " world" );
        second.copyFormatting( first, ComponentBuilder.FormatRetention.ALL, true );
        assertEquals( first.isBold(), second.isBold() );
        assertEquals( first.getColor(), second.getColor() );
        assertEquals( first.getClickEvent(), second.getClickEvent() );
        assertEquals( first.getHoverEvent(), second.getHoverEvent() );
    }
    private static <T> void testBuilderFormatRetention(Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter)
    {
        T noneRetention = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .append( "World", ComponentBuilder.FormatRetention.NONE ) );

        assertEquals( RED, extraGetter.apply( noneRetention, 0 ).getColor() );
        assertEquals( WHITE, extraGetter.apply( noneRetention, 1 ).getColor() );

        HoverEvent testEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new ComponentBuilder( "test" ).build() ) );

        T formattingRetention = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .event( testEvent ).append( "World", ComponentBuilder.FormatRetention.FORMATTING ) );

        assertEquals( RED, extraGetter.apply( formattingRetention, 0 ).getColor() );
        assertEquals( testEvent, extraGetter.apply( formattingRetention, 0 ).getHoverEvent() );
        assertEquals( RED, extraGetter.apply( formattingRetention, 1 ).getColor() );
        assertNull( extraGetter.apply( formattingRetention, 1 ).getHoverEvent() );

        ClickEvent testClickEvent = new ClickEvent( ClickEvent.Action.OPEN_URL, "http://www.example.com" );

        T eventRetention = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .event( testEvent ).event( testClickEvent ).append( "World", ComponentBuilder.FormatRetention.EVENTS ) );

        assertEquals( RED, extraGetter.apply( eventRetention, 0 ).getColor() );
        assertEquals( testEvent, extraGetter.apply( eventRetention, 0 ).getHoverEvent() );
        assertEquals( testClickEvent, extraGetter.apply( eventRetention, 0 ).getClickEvent() );
        assertEquals( WHITE, extraGetter.apply( eventRetention, 1 ).getColor() );
        assertEquals( testEvent, extraGetter.apply( eventRetention, 1 ).getHoverEvent() );
        assertEquals( testClickEvent, extraGetter.apply( eventRetention, 1 ).getClickEvent() );
    }

}
