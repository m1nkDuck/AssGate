import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.lang.reflect.Field;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
/** Verifies basement compositing and telegraph readability through real MIDP graphics. */
public class ArenaArtTest {
    static final int WIDTH=320,HEIGHT=240,BACKGROUND=0x061119;
    static void check(boolean condition,String message){if(!condition)throw new RuntimeException(message);}
    static Image image(){return Image.createImage(WIDTH,HEIGHT);}
    static int[] pixels(Image image){int[] rgb=new int[WIDTH*HEIGHT];image.getRGB(rgb,0,WIDTH,0,0,WIDTH,HEIGHT);return rgb;}
    static long hash(Image image){return DirectionTest.hash(image,0,0,WIDTH,HEIGHT);}
    static Image layer(Art art,String name)throws Exception{
        Field field=Art.class.getDeclaredField(name);field.setAccessible(true);return (Image)field.get(art);
    }
    static World fight(){
        World w=new World();w.mode=World.FIGHT;w.bstate=World.RECOVER;
        w.px=80;w.py=197;w.fx=0;w.fy=1;w.bx=170;w.by=168;w.bdx=0;w.bdy=1;
        w.clock=1500;w.phase=1;w.phaseBanner=0;w.events=0;return w;
    }
    static Image frame(Art art,World w,boolean paused){Image image=image();art.draw(image.getGraphics(),w,paused);return image;}
    static Image actors(Art art,World w){
        Image image=image();Graphics g=image.getGraphics();g.setColor(BACKGROUND);g.fillRect(0,0,WIDTH,HEIGHT);
        if(w.py<w.by){art.hero(g,w);art.boss(g,w);}else{art.boss(g,w);art.hero(g,w);}return image;
    }
    static void actorsPreserved(Art art,World w,Image scene){
        int[] expected=pixels(actors(art,w)),actual=pixels(scene);int tested=0;
        for(int y=49;y<217;y++)for(int x=16;x<304;x++){
            int i=y*WIDTH+x;if((expected[i]&0xffffff)!=BACKGROUND){
                check(expected[i]==actual[i],"Room decoration or ambient covers an actor at "+x+","+y);tested++;
            }
        }
        check(tested>=1500,"Actor compositing check missed the hero or boss");
    }
    static Image ambient(Image floor,int clock){
        Image image=image();Graphics g=image.getGraphics();g.drawImage(floor,0,0,Graphics.TOP|Graphics.LEFT);ArenaArt.ambient(g,clock);return image;
    }
    static void stableCompositing(Art art)throws Exception{
        Image floor=layer(art,"floor");check(floor.getWidth()==WIDTH&&floor.getHeight()==HEIGHT,"Room floor has wrong dimensions");
        long floorHash=hash(floor);check(floorHash==hash(ArenaArt.create()),"Art does not use the new arena background");
        int[] floorPixels=pixels(floor);for(int i=0;i<floorPixels.length;i++)check((floorPixels[i]>>>24)==255,"Room background leaves transparent gaps");
        check(hash(ambient(floor,0))!=hash(ambient(floor,1234)),"Room ambient never animates");
        World w=fight();Image expected=frame(art,w,false);long first=hash(expected);actorsPreserved(art,w,expected);
        Image reused=image();
        for(int i=0;i<4;i++){art.draw(reused.getGraphics(),w,false);check(hash(reused)==first,"Repeated drawing accumulates ambient pixels");}
        w.clock=4721;Image later=frame(art,w,false);w.clock=1500;
        check(hash(frame(art,w,false))==first,"Advancing and restoring the clock leaves old ambient behind");
        check(hash(layer(art,"floor"))==floorHash,"Ambient drawing mutates the cached floor");
        check(DirectionTest.hash(expected,0,0,320,26)==DirectionTest.hash(later,0,0,320,26),"Ambient overwrites the top HUD");
        check(DirectionTest.hash(expected,25,224,270,16)==DirectionTest.hash(later,25,224,270,16),"Ambient overwrites the boss HP bar");
        Image paused=frame(art,w,true);check(hash(paused)!=first,"Pause panel is missing");
        check(hash(paused)==hash(frame(art,w,true)),"Paused room changes at a frozen clock");
        check(hash(frame(art,w,false))==first,"Pause panel persists after resuming");
        save(expected,"arena-basement-test");save(paused,"arena-basement-paused-test");
    }
    static int verifyMask(Image mask,World w){
        check(mask.getWidth()==WIDTH&&mask.getHeight()==HEIGHT,"Telegraph mask has wrong dimensions");
        int[] colors=pixels(mask);int marked=0,hazard=0,boundary=0;
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            int color=colors[y*WIDTH+x],alpha=color>>>24;
            boolean inside=x>=16&&x<304&&y>=49&&y<217&&w.inHazard(x,y,false);
            check(alpha==0||alpha==255,"Telegraph alpha is not binary");
            if(alpha!=0){check(inside,"Telegraph marks a safe pixel for attack "+w.attack+" at "+x+","+y);marked++;}
            if(inside){
                hazard++;boolean edge=!w.inHazard(x-1,y,false)||!w.inHazard(x+1,y,false)||!w.inHazard(x,y-1,false)||!w.inHazard(x,y+1,false);
                if(edge){check(alpha==255,"Telegraph omits a hazard boundary for attack "+w.attack);boundary++;}
            }
        }
        check(boundary>=25&&marked>=hazard/8,"Hazard outline or interior warning is unreadable for attack "+w.attack);return marked;
    }
    static void telegraphs(Art art)throws Exception{
        World w=fight();long floorHash=hash(layer(art,"floor"));
        for(int attack=0;attack<4;attack++){
            w.attack=attack;w.bx=115;w.by=150;w.px=220;w.py=195;w.lockAttack();w.events=0;
            art.telegraph(w);Image initial=layer(art,"warned");long maskHash=hash(initial);verifyMask(initial,w);
            art.telegraph(w);check(initial==layer(art,"warned")&&maskHash==hash(initial),"Same hazard version rebuilds or changes its cached mask");
            Image warning=frame(art,w,false);actorsPreserved(art,w,warning);
            int[] mask=pixels(initial),full=pixels(warning),foreground=pixels(actors(art,w));int checked=0;
            for(int i=0;i<mask.length;i++)if((mask[i]>>>24)!=0&&(foreground[i]&0xffffff)==BACKGROUND){
                check(mask[i]==full[i],"Room scenery hides a warning pixel for attack "+attack);checked++;
            }
            check(checked>=25,"Actors obscure all warning samples for attack "+attack);
            w.clock+=997;frame(art,w,false);check(hash(initial)==maskHash,"Ambient is burned into the warning cache");
            int[] previous=pixels(initial);w.bx=210;w.by=150;w.px=110;w.py=190;w.lockAttack();w.events=0;
            art.telegraph(w);Image moved=layer(art,"warned");verifyMask(moved,w);int[] updated=pixels(moved);int cleared=0;
            for(int y=49;y<217;y++)for(int x=16;x<304;x++){
                int i=y*WIDTH+x;if((previous[i]>>>24)!=0&&!w.inHazard(x,y,false)){
                    check((updated[i]>>>24)==0,"New hazard version retains a stale warning pixel");cleared++;
                }
            }
            check(cleared>=25,"Cache invalidation did not exercise a changed hazard area");
            w.bstate=World.ACTIVE;w.bt=Balance.ACTIVE[attack]/2;frame(art,w,false);
            check(hash(layer(art,"floor"))==floorHash,"Hazard rendering modifies the cached arena");
        }
    }
    static void save(Image image,String name)throws Exception{
        BufferedImage output=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);output.setRGB(0,0,WIDTH,HEIGHT,pixels(image),0,WIDTH);
        ImageIO.write(output,"png",new File("preview/"+name+".png"));
    }
    static void runTests()throws Exception{
        RenderTest context=new RenderTest();DeviceImpl device=DeviceImpl.create(context,ArenaArtTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);
        DeviceFactory.setDevice(device);device.init();new File("preview").mkdirs();Art art=new Art();stableCompositing(art);telegraphs(art);
        System.out.println("PASS: Damp basement stays behind actors, preserves HUD, and redraws without accumulating ambient at running or paused clocks.");
        System.out.println("PASS: Four transparent telegraph masks match collision boundaries, remain visible above scenery, and invalidate without stale pixels.");
    }
    public static void main(String[] args){try{runTests();System.exit(0);}catch(Throwable error){error.printStackTrace();System.exit(1);}}
}
