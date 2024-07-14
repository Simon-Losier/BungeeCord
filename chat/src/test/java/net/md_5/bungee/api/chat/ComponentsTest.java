package net.md_5.bungee.api.chat;

import static net.md_5.bungee.api.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

public class ComponentsTest
{

    public static void testDissembleReassemble(BaseComponent[] components)
    {
        String json = ComponentSerializer.toString( components );
        BaseComponent[] parsed = ComponentSerializer.parse( json );
        assertEquals( BaseComponent.toLegacyText( parsed ), BaseComponent.toLegacyText( components ) );
    }

    public static void testDissembleReassemble(BaseComponent component)
    {
        String json = ComponentSerializer.toString( component );
        BaseComponent[] parsed = ComponentSerializer.parse( json );
        assertEquals( BaseComponent.toLegacyText( parsed ), BaseComponent.toLegacyText( component ) );
    }

    public static void testAssembleDissemble(String json, boolean modern)
    {
        if ( modern )
        {
            BaseComponent deserialized = ComponentSerializer.deserialize( json );
            assertEquals( json, ComponentSerializer.toString( deserialized ) );
        } else
        {
            BaseComponent[] parsed = ComponentSerializer.parse( json );
            assertEquals( json, ComponentSerializer.toString( parsed ) );
        }
    }

    @Test
    public void testItemParse()
    {
        // Declare all commonly used variables for reuse.
        BaseComponent[] components;
        TextComponent textComponent;
        String json;

        textComponent = new TextComponent( "Test" );
        textComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_ITEM, new BaseComponent[]
        {
            new TextComponent( "{id:\"minecraft:netherrack\",Count:47b}" )
        } ) );
        testDissembleReassemble( new BaseComponent[]
        {
            textComponent
        } );
        testDissembleReassemble( textComponent );
        json = "{\"hoverEvent\":{\"action\":\"show_item\",\"value\":[{\"text\":\"{id:\\\"minecraft:netherrack\\\",Count:47b}\"}]},\"text\":\"Test\"}";
        testAssembleDissemble( json, false );
        testAssembleDissemble( json, true );
        //////////
        String hoverVal = "{\"text\":\"{id:\\\"minecraft:dirt\\\",Count:1b}\"}";
        json = "{\"extra\":[{\"text\":\"[\"},{\"extra\":[{\"translate\":\"block.minecraft.dirt\"}],\"text\":\"\"},{\"text\":\"]\"}],\"hoverEvent\":{\"action\":\"show_item\",\"value\":[" + hoverVal + "]},\"text\":\"\"}";
        components = ComponentSerializer.parse( json );
        Text contentText = ( (Text) components[0].getHoverEvent().getContents().get( 0 ) );
        assertEquals( hoverVal, ComponentSerializer.toString( (BaseComponent[]) contentText.getValue() ) );
        testDissembleReassemble( components );
        //////////
        // TODO: now ambiguous since "text" to distinguish Text from Item is not required
        /*
        TextComponent component1 = new TextComponent( "HoverableText" );
        String nbt = "{display:{Name:{text:Hello},Lore:[{text:Line_1},{text:Line_2}]},ench:[{id:49,lvl:5}],Unbreakable:1}}";
        Item contentItem = new Item( "minecraft:wood", 1, ItemTag.ofNbt( nbt ) );
        HoverEvent hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_ITEM, contentItem );
        component1.setHoverEvent( hoverEvent );
        json = ComponentSerializer.toString( component1 );
        components = ComponentSerializer.parse( json );
        Item parsedContentItem = ( (Item) components[0].getHoverEvent().getContents().get( 0 ) );
        assertEquals( contentItem, parsedContentItem );
        assertEquals( contentItem.getCount(), parsedContentItem.getCount() );
        assertEquals( contentItem.getId(), parsedContentItem.getId() );
        assertEquals( nbt, parsedContentItem.getTag().getNbt() );
         */
    }

    @Test
    public void testArrayUUIDParse()
    {
        BaseComponent[] uuidComponent = ComponentSerializer.parse( "{\"translate\":\"multiplayer.player.joined\",\"with\":[{\"text\":\"Rexcantor64\",\"hoverEvent\":{\"contents\":{\"type\":\"minecraft:player\",\"id\":[1328556382,-2138814985,-1895806765,-1039963041],\"name\":\"Rexcantor64\"},\"action\":\"show_entity\"},\"insertion\":\"Rexcantor64\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell Rexcantor64 \"}}],\"color\":\"yellow\"}" );
        assertEquals( "4f30295e-8084-45f7-8f00-48d3c2036c5f", ( (Entity) ( (TranslatableComponent) uuidComponent[0] ).getWith().get( 0 ).getHoverEvent().getContents().get( 0 ) ).getId() );
        testDissembleReassemble( uuidComponent );
    }





    @Test
    public void testDummyRetaining()
    {
        ComponentBuilder builder = new ComponentBuilder();
        assertNotNull( builder.getCurrentComponent() );
        builder.color( GREEN );
        builder.append( "test ", ComponentBuilder.FormatRetention.ALL );
        assertEquals( builder.getCurrentComponent().getColor(), GREEN );
    }

    @Test
    public void testComponentGettingExceptions()
    {
        ComponentBuilder builder = new ComponentBuilder();
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( -1 ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( 0 ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( 1 ) );
        BaseComponent component = new TextComponent( "Hello" );
        builder.append( component );
        assertEquals( builder.getComponent( 0 ), component );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( 1 ) );
    }



    @Test
    public void testComponentParting()
    {
        ComponentBuilder builder = new ComponentBuilder();
        TextComponent apple = new TextComponent( "apple" );
        builder.append( apple );
        assertEquals( builder.getCurrentComponent(), apple );
        assertEquals( builder.getComponent( 0 ), apple );

        TextComponent mango = new TextComponent( "mango" );
        TextComponent orange = new TextComponent( "orange" );
        builder.append( mango );
        builder.append( orange );
        builder.removeComponent( 1 ); // Removing mango
        assertEquals( builder.getComponent( 0 ), apple );
        assertEquals( builder.getComponent( 1 ), orange );
    }







    /*
    @Test
    public void testItemTag()
    {
        TextComponent component = new TextComponent( "Hello world" );
        HoverEvent.ContentItem content = new HoverEvent.ContentItem();
        content.setId( "minecraft:diamond_sword" );
        content.setCount( 1 );
        content.setTag( ItemTag.builder()
                .ench( new ItemTag.Enchantment( 5, 16 ) )
                .name( new TextComponent( "Sharp Sword" ) )
                .unbreakable( true )
                .lore( new ComponentBuilder( "Line1" ).create() )
                .lore( new ComponentBuilder( "Line2" ).create() )
                .build() );
        HoverEvent event = new HoverEvent( HoverEvent.Action.SHOW_ITEM, content );
        component.setHoverEvent( event );
        String serialised = ComponentSerializer.toString( component );
        BaseComponent[] deserialised = ComponentSerializer.parse( serialised );
        assertEquals( TextComponent.toLegacyText( deserialised ), TextComponent.toLegacyText( component ) );
    }
     */

    @Test
    public void testModernShowAdvancement()
    {
        String advancement = "achievement.openInventory";
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( advancement )
        );
        TextComponent component = new TextComponent( "test" );
        component.setHoverEvent( hoverEvent );
        assertEquals( component.getHoverEvent().getContents().size(), 1 );
        assertTrue( component.getHoverEvent().getContents().get( 0 ) instanceof Text );
        assertEquals( ( (Text) component.getHoverEvent().getContents().get( 0 ) ).getValue(), advancement );
    }







    @Test
    public void testScore()
    {
        BaseComponent[] component = ComponentSerializer.parse( "{\"score\":{\"name\":\"@p\",\"objective\":\"TEST\",\"value\":\"hello\"}}" );
        String text = ComponentSerializer.toString( component );
        BaseComponent[] reparsed = ComponentSerializer.parse( text );

        assertArrayEquals( component, reparsed );
    }

    @Test
    public void testStyle()
    {
        ComponentStyle style = ComponentSerializer.deserializeStyle( "{\"color\":\"red\",\"font\":\"minecraft:example\",\"bold\":true,\"italic\":false,\"obfuscated\":true}" );
        String text = ComponentSerializer.toString( style );
        ComponentStyle reparsed = ComponentSerializer.deserializeStyle( text );

        assertEquals( style, reparsed );
    }

    @Test
    public void testBasicComponent()
    {
        TextComponent textComponent = new TextComponent( "Hello world" );
        textComponent.setColor( RED );

        assertEquals( "Hello world", textComponent.toPlainText() );
        assertEquals( RED + "Hello world", textComponent.toLegacyText() );
    }

    @Test
    public void testLoopSimple()
    {
        TextComponent component = new TextComponent( "Testing" );
        component.addExtra( component );
        assertThrows( IllegalArgumentException.class, () -> ComponentSerializer.toString( component ) );
    }

    @Test
    public void testLoopComplex()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( RED );
        a.addExtra( b );
        b.addExtra( c );
        c.addExtra( a );
        assertThrows( IllegalArgumentException.class, () -> ComponentSerializer.toString( a ) );
    }

    @Test
    public void testRepeated()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( AQUA );
        a.addExtra( b );
        a.addExtra( b );
        ComponentSerializer.toString( a );
    }

    @Test
    public void testRepeatedError()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( RED );
        a.addExtra( b );
        a.addExtra( c );
        c.addExtra( a );
        a.addExtra( b );
        assertThrows( IllegalArgumentException.class, () -> ComponentSerializer.toString( a ) );
    }







    @Test
    public void testEquals()
    {
        TextComponent first = new TextComponent( "Hello, " );
        first.addExtra( new TextComponent( "World!" ) );

        TextComponent second = new TextComponent( "Hello, " );
        second.addExtra( new TextComponent( "World!" ) );

        assertEquals( first, second );
    }

    @Test
    public void testNotEquals()
    {
        TextComponent first = new TextComponent( "Hello, " );
        first.addExtra( new TextComponent( "World." ) );

        TextComponent second = new TextComponent( "Hello, " );
        second.addExtra( new TextComponent( "World!" ) );

        assertNotEquals( first, second );
    }



    @Test
    public void testStyleIsEmpty()
    {
        ComponentStyle style = ComponentStyle.builder().build();
        assertTrue( style.isEmpty() );

        style = ComponentStyle.builder()
                .bold( true )
                .build();
        assertFalse( style.isEmpty() );
    }


}
