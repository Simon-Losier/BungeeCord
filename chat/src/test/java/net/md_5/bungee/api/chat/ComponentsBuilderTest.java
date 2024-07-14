package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.function.*;

import static net.md_5.bungee.api.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComponentsBuilderTest {

    @Test
    public void testEmptyComponentBuilderBuild()
    {
        testEmptyComponentBuilder(
                ComponentBuilder::build,
                (component) -> assertNull( component.getExtra() ),
                (component, size) -> assertEquals( component.getExtra().size(), size )
        );
    }

    @Test
    public void testEmptyComponentBuilderCreate()
    {
        testEmptyComponentBuilder(
                ComponentBuilder::create,
                (components) -> assertEquals( components.length, 0 ),
                (components, size) -> assertEquals( size, components.length )
        );
    }

    @Test
    public void testBuilderAppendBuild()
    {
        testBuilderAppend(
                () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new ComponentBuilder( "Hello world" ).build() ) ),
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index ),
                (component) -> BaseComponent.toPlainText( component ),
                // An extra format code is appended to the beginning because there is an empty TextComponent at the start of every component
                WHITE.toString() + YELLOW + "Hello " + GREEN + "world!",
                (component) -> BaseComponent.toLegacyText( component )
        );
    }
    @Test
    public void testBuilderAppendCreate()
    {
        testBuilderAppend(
                () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Hello world" ).create() ),
                ComponentBuilder::create,
                (components, index) -> components[index],
                BaseComponent::toPlainText,
                YELLOW + "Hello " + GREEN + "world!",
                BaseComponent::toLegacyText
        );
    }

    @Test
    public void testBuilderAppendCreateMixedComponents()
    {
        testBuilderAppendMixedComponents(
                ComponentBuilder::create,
                (components, index) -> components[index]
        );
    }

    @Test
    public void testBuilderAppendBuildMixedComponents()
    {
        testBuilderAppendMixedComponents(
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index )
        );
    }


    @Test
    public void testComponentBuilderCursorInvalidPos()
    {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append( new TextComponent( "Apple, " ) );
        builder.append( new TextComponent( "Orange, " ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.setCursor( -1 ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.setCursor( 2 ) );
    }

    @Test
    public void testComponentBuilderCursor()
    {
        TextComponent t1, t2, t3;
        ComponentBuilder builder = new ComponentBuilder();
        assertEquals( builder.getCursor(), -1 );
        builder.append( t1 = new TextComponent( "Apple, " ) );
        assertEquals( builder.getCursor(), 0 );
        builder.append( t2 = new TextComponent( "Orange, " ) );
        builder.append( t3 = new TextComponent( "Mango, " ) );
        assertEquals( builder.getCursor(), 2 );

        builder.setCursor( 0 );
        assertEquals( builder.getCurrentComponent(), t1 );

        // Test that appending new components updates the position to the new list size
        // after having previously set it to 0 (first component)
        builder.append( new TextComponent( "and Grapefruit" ) );
        assertEquals( builder.getCursor(), 3 );

        builder.setCursor( 0 );
        builder.resetCursor();
        assertEquals( builder.getCursor(), 3 );
    }


    @Test
    public void testBuilderCreateReset()
    {
        testBuilderReset(
                ComponentBuilder::create,
                (components, index) -> components[index]
        );
    }

    @Test
    public void testBuilderBuildReset()
    {
        testBuilderReset(
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index )
        );
    }

    @Test
    public void testBuilderCreate()
    {
        testBuilder(
                ComponentBuilder::create,
                BaseComponent::toPlainText,
                RED + "Hello " + BLUE + BOLD + "World" + YELLOW + BOLD + "!",
                BaseComponent::toLegacyText
        );
    }

    @Test
    public void testBuilderBuild()
    {
        testBuilder(
                ComponentBuilder::build,
                (component) -> BaseComponent.toPlainText( component ),
                // An extra format code is appended to the beginning because there is an empty TextComponent at the start of every component
                WHITE.toString() + RED + "Hello " + BLUE + BOLD + "World" + YELLOW + BOLD + "!",
                (component) -> BaseComponent.toLegacyText( component )
        );
    }
    @Test
    public void testHoverEventContentsCreate()
    {
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( new ComponentBuilder( "First" ).create() ),
                new Text( new ComponentBuilder( "Second" ).create() )
        );

        this.testHoverEventContents(
                hoverEvent,
                ComponentSerializer::parse,
                (components) -> components[0].getHoverEvent(),
                ComponentsTest::testDissembleReassemble // BaseComponent
        );

        // check the test still works with the value method
        hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Sample text" ).create() );
        TextComponent component = new TextComponent( "Sample text" );
        component.setHoverEvent( hoverEvent );

        assertEquals( hoverEvent.getContents().size(), 1 );
        assertTrue( hoverEvent.isLegacy() );
        String serialized = ComponentSerializer.toString( component );
        BaseComponent[] deserialized = ComponentSerializer.parse( serialized );
        assertEquals( component.getHoverEvent(), deserialized[0].getHoverEvent() );
    }

    @Test
    public void testBuilderCloneCreate()
    {
        testBuilderClone( (builder) -> BaseComponent.toLegacyText( builder.create() ) );
    }

    @Test
    public void testBuilderCloneBuild()
    {
        testBuilderClone( (builder) -> BaseComponent.toLegacyText( builder.build() ) );
    }

    @Test
    public void testHoverEventContentsBuild()
    {
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( new ComponentBuilder( "First" ).build() ),
                new Text( new ComponentBuilder( "Second" ).build() )
        );

        this.testHoverEventContents(
                hoverEvent,
                ComponentSerializer::deserialize,
                BaseComponent::getHoverEvent,
                ComponentsTest::testDissembleReassemble // BaseComponent
        );
    }


    private static <T> void testBuilderAppend(Supplier<HoverEvent> hoverEventSupplier, Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter, Function<T, String> toPlainTextFunction, String expectedLegacyText, Function<T, String> toLegacyTextFunction)
    {
        ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/help " );
        HoverEvent hoverEvent = hoverEventSupplier.get();

        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( YELLOW );
        builder.append( new ComponentBuilder( "world!" ).color( GREEN ).event( hoverEvent ).event( clickEvent ).create() ); // Intentionally using create() to append multiple individual components

        T component = componentBuilder.apply( builder );

        assertEquals( extraGetter.apply( component, 1 ).getHoverEvent(), hoverEvent );
        assertEquals( extraGetter.apply( component, 1 ).getClickEvent(), clickEvent );
        assertEquals( "Hello world!", toPlainTextFunction.apply( component ) );
        assertEquals( expectedLegacyText, toLegacyTextFunction.apply( component ) );
    }
    private static <T> void testEmptyComponentBuilder(Function<ComponentBuilder, T> componentBuilder, Consumer<T> emptyAssertion, ObjIntConsumer<T> sizedAssertion)
    {
        ComponentBuilder builder = new ComponentBuilder();

        T component = componentBuilder.apply( builder );
        emptyAssertion.accept( component );

        for ( int i = 0; i < 3; i++ )
        {
            builder.append( "part:" + i );
            component = componentBuilder.apply( builder );
            sizedAssertion.accept( component, i + 1 );
        }
    }
    private static <T> void testBuilderAppendMixedComponents(Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter)
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " );
        TextComponent textComponent = new TextComponent( "world " );
        TranslatableComponent translatableComponent = new TranslatableComponent( "item.swordGold.name" );
        // array based BaseComponent append
        builder.append( new BaseComponent[]
                {
                        textComponent,
                        translatableComponent
                } );
        ScoreComponent scoreComponent = new ScoreComponent( "myscore", "myobjective" );
        builder.append( scoreComponent ); // non array based BaseComponent append
        T component = componentBuilder.apply( builder );
        assertEquals( "Hello ", extraGetter.apply( component, 0 ).toPlainText() );
        assertEquals( textComponent.toPlainText(), extraGetter.apply( component, 1 ).toPlainText() );
        assertEquals( translatableComponent.toPlainText(), extraGetter.apply( component, 2 ).toPlainText() );
        assertEquals( scoreComponent.toPlainText(), extraGetter.apply( component, 3 ).toPlainText() );
    }


    private static <T> void testBuilderReset(Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter)
    {
        T component = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .append( "World" ).reset() );

        assertEquals( RED, extraGetter.apply( component, 0 ).getColor() );
        assertEquals( WHITE, extraGetter.apply( component, 1 ).getColor() );
    }
    private static <T> void testBuilder(Function<ComponentBuilder, T> componentBuilder, Function<T, String> toPlainTextFunction, String expectedLegacyString, Function<T, String> toLegacyTextFunction)
    {
        T component = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED ).
                append( "World" ).bold( true ).color( BLUE ).
                append( "!" ).color( YELLOW ) );

        assertEquals( "Hello World!", toPlainTextFunction.apply( component ) );
        assertEquals( expectedLegacyString, toLegacyTextFunction.apply( component ) );
    }
    private static void testBuilderClone(Function<ComponentBuilder, String> legacyTextFunction)
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( RED ).append( "world" ).color( DARK_RED );
        ComponentBuilder cloned = new ComponentBuilder( builder );

        assertEquals( legacyTextFunction.apply( builder ), legacyTextFunction.apply( cloned ) );
    }

    private <T> void testHoverEventContents(HoverEvent hoverEvent, Function<String, T> deserializer, Function<T, HoverEvent> hoverEventGetter, Consumer<T> dissembleReassembleTest)
    {
        TextComponent component = new TextComponent( "Sample text" );
        component.setHoverEvent( hoverEvent );
        assertEquals( hoverEvent.getContents().size(), 2 );
        assertFalse( hoverEvent.isLegacy() );

        String serialized = ComponentSerializer.toString( component );
        T deserialized = deserializer.apply( serialized );
        assertEquals( component.getHoverEvent(), hoverEventGetter.apply( deserialized ) );

        // Test single content:
        String json = "{\"italic\":true,\"color\":\"gray\",\"translate\":\"chat.type.admin\",\"with\":[{\"text\":\"@\"}"
                + ",{\"translate\":\"commands.give.success.single\",\"with\":[\"1\",{\"color\":\"white\""
                + ",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:diamond_sword\",\"tag\":\""
                + "{Damage:0,display:{Lore:['\\\"test lore'!\\\"'],Name:'\\\"test\\\"'}}\"}},"
                + "\"extra\":[{\"italic\":true,\"extra\":[{\"text\":\"test\"}],\"text\":\"\"},{\"text\":\"]\"}],"
                + "\"text\":\"[\"},{\"insertion\":\"Name\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":"
                + "\"/tell Name \"},\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":"
                + "{\"type\":\"minecraft:player\",\"id\":\"00000000-0000-0000-0000-00000000000000\",\"name\":"
                + "{\"text\":\"Name\"}}},\"text\":\"Name\"}]}]}";
        dissembleReassembleTest.accept( deserializer.apply( json ) );
    }

}
