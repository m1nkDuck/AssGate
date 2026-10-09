import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.util.Arrays;

/** Real MIDP frames verify dark-room warnings, scenery caches and chapter transitions. */
public final class BelfryRenderTest {
    static final int BACKGROUND=0x061119;
    static int checks,frames;
    static void check(boolean value,String message){if(!value)throw new RuntimeException(message);checks++;System.out.println("PASS: "+message);}
    static World chapter(int room){
        World w=new World();w.oathkeeperDefeated=w.oathkeeperComplete=true;w.belfry.enterRoom(w,room,true);
        w.clock=1700;w.belfry.roomTime=4000;w.belfry.entranceQuoteTime=0;w.px=221;w.py=187;w.fx=-1;w.fy=0;return w;
    }
    static Image frame(Art art,World w,String name)throws Exception{
        Image im=ArenaArtTest.frame(art,w,false);ArenaArtTest.save(im,name);frames++;return im;
    }
    static void mask(Art art,World w)throws Exception{
        Image scene=ArenaArtTest.frame(art,w,false),layer=ArenaArtTest.layer(art,"belfryWarned");
        int[] rgb=ArenaArtTest.pixels(layer),full=ArenaArtTest.pixels(scene);
        Image actors=Image.createImage(320,240);Graphics g=actors.getGraphics();g.setColor(BACKGROUND);g.fillRect(0,0,320,240);
        for(int i=0;i<2;i++)if(w.belfry.enemies[i].active)BelfryArt.guard(g,w.belfry.enemies[i],w.clock);
        art.hero(g,w);if(w.belfry.room==4)BellboundArt.draw(g,w.belfry.boss,w.clock);
        int[] front=ArenaArtTest.pixels(actors);int edges=0,visible=0;
        for(int y=0;y<240;y++)for(int x=0;x<320;x++){
            int i=y*320+x,c=rgb[i];boolean inside=x>=16&&x<304&&y>=49&&y<217&&w.belfry.hazardAt(x,y);
            if((c>>>24)!=0){
                if(!inside)throw new RuntimeException("Warning covers a safe pixel "+x+","+y);
                if(c!=0xffffd78c&&c!=0xff963e3b)throw new RuntimeException("Warning is dimmed by darkness");
                if(y>=75&&y<216&&(front[i]&0xffffff)==BACKGROUND){
                    if(c!=full[i])throw new RuntimeException("Scenery covers warning at "+x+","+y);visible++;
                }
            }
            if(inside&&(!w.belfry.hazardAt(x-1,y)||!w.belfry.hazardAt(x+1,y)||!w.belfry.hazardAt(x,y-1)||!w.belfry.hazardAt(x,y+1))){
                if(c!=0xffffd78c)throw new RuntimeException("Warning boundary missing at "+x+","+y);edges++;
            }
        }
        check(edges>=20&&visible>=35,"Disclosed hazard geometry remains bright and visible over the dark scenery");
        Image original=layer;ArenaArtTest.frame(art,w,false);
        check(original==ArenaArtTest.layer(art,"belfryWarned"),"An unchanged hazard reuses its raster cache");
    }
    static void runTests()throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,BelfryRenderTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        Art art=new Art();World w=new World();w.oathkeeperComplete=true;w.belfry.start(w);w.update(600,0,0,false,false,false);
        int remaining=w.belfry.entranceQuoteTime;int[] quote=ArenaArtTest.pixels(frame(art,w,"belfry-entrance-quote"));
        w.belfry.entranceQuoteTime=0;int[] noQuote=ArenaArtTest.pixels(frame(art,w,"belfry-entrance-quote-expired"));int changed=0,minY=240,maxY=0;
        for(int y=0;y<240;y++)for(int x=0;x<320;x++)if(quote[y*320+x]!=noQuote[y*320+x]){changed++;minY=Math.min(minY,y);maxY=Math.max(maxY,y);}
        check(remaining>0&&changed>0&&minY>=120&&maxY<216&&maxY-minY<40,"The small entrance caption stays in the lower half without covering the HUD or upper scene");
        w.belfry.enterRoom(w,1,true);w.belfry.enterRoom(w,0,true);
        check(Arrays.equals(noQuote,ArenaArtTest.pixels(frame(art,w,"belfry-entrance-return"))),"Reentering the room at time zero renders without the already-seen caption");
        long[] floors=new long[5];
        for(int room=0;room<5;room++){
            w=chapter(room);if(room==3){w.belfry.fireMask=7;w.belfry.guardianDefeated=true;}
            frame(art,w,"belfry-room-"+room);Image floor=ArenaArtTest.layer(art,"belfryFloor");floors[room]=ArenaArtTest.hash(floor);
            int[] first=ArenaArtTest.pixels(ArenaArtTest.frame(art,w,false));
            check(Arrays.equals(first,ArenaArtTest.pixels(ArenaArtTest.frame(art,w,false))),"Room "+room+" does not accumulate ash, flame or drawing remnants");
            check(floors[room]==ArenaArtTest.hash(floor),"Room "+room+" preserves its cached background while drawing");
            for(int prior=0;prior<room;prior++)if(floors[prior]==floors[room])throw new RuntimeException("Two chapter rooms share the same scenery");
        }
        check(true,"All five chapter screens have distinct scenery");
        w=chapter(1);Belfry.Enemy e=w.belfry.enemies[0];e.x=e.ox=103;e.y=e.oy=142;e.dx=0;e.dy=1;e.state=World.WARNING;e.time=100;e.lit=false;w.belfry.hazardVersion++;
        frame(art,w,"belfry-dark-warning");mask(art,w);Image darkFloor=ArenaArtTest.layer(art,"belfryFloor");
        w.belfry.fireMask=1;e.lit=true;frame(art,w,"belfry-lit-warning");mask(art,w);
        check(ArenaArtTest.hash(darkFloor)!=ArenaArtTest.hash(ArenaArtTest.layer(art,"belfryFloor")),"Lighting a brazier rebuilds the warm floor and reveals the wall scratches");
        Image guardDark=Image.createImage(320,240),guardLit=Image.createImage(320,240);e.lit=false;BelfryArt.guard(guardDark.getGraphics(),e,w.clock);e.lit=true;BelfryArt.guard(guardLit.getGraphics(),e,w.clock);
        check(ArenaArtTest.hash(guardDark)!=ArenaArtTest.hash(guardLit),"Hidden and revealed guards have visibly different armour");
        w.belfry.notice=Belfry.FIRE_LIT;w.belfry.noticeTime=2000;mask(art,w);frame(art,w,"belfry-light-lore");
        e.state=World.SEEK;w.belfry.hazardVersion++;ArenaArtTest.frame(art,w,false);int[] empty=ArenaArtTest.pixels(ArenaArtTest.layer(art,"belfryWarned"));
        for(int i=0;i<empty.length;i++)if((empty[i]>>>24)!=0)throw new RuntimeException("A cleared attack retains warning pixels");
        check(true,"Ending a guard attack clears all stale warning pixels");
        for(int attack=0;attack<3;attack++){
            w=chapter(4);Bellbound b=w.belfry.boss;b.x=b.ox=160;b.y=b.oy=145;b.dx=0;b.dy=1;b.attack=attack;b.tx=180;b.ty=179;b.state=World.WARNING;b.time=200;b.ringIndex=2;
            if(attack==Bellbound.TOLL){b.ox=160;b.oy=138;}b.hazardVersion++;
            frame(art,w,"bellbound-warning-"+attack);mask(art,w);
        }
        w=chapter(4);Bellbound b=w.belfry.boss;b.hit(300);b.update(w,1);frame(art,w,"bellbound-falling-start");
        long withoutRoofBell=ArenaArtTest.hash(ArenaArtTest.layer(art,"belfryFloor"));
        b.update(w,550);frame(art,w,"bellbound-falling-mid");b.update(w,550);frame(art,w,"bellbound-phase-two");
        check(b.fallenBell&&withoutRoofBell==ArenaArtTest.hash(ArenaArtTest.layer(art,"belfryFloor")),"The roof bell disappears when it falls and does not reappear above the new obstacle");
        w.belfry.enterRoom(w,4,true);frame(art,w,"bellbound-retry");
        check(withoutRoofBell!=ArenaArtTest.hash(ArenaArtTest.layer(art,"belfryFloor")),"Retrying the boss restores the hanging roof bell and initial arena");
        w.belfry.boss.hit(600);w.update(1,0,0,false,false,false);w.deathTime=3100;frame(art,w,"belfry-dawn-shard");
        Image dawnA=Image.createImage(320,240),dawnB=Image.createImage(320,240);BelfryArt.dawn(dawnA.getGraphics(),0,true);BelfryArt.dawn(dawnB.getGraphics(),1200,true);
        check(ArenaArtTest.hash(dawnA)!=ArenaArtTest.hash(dawnB),"The false dawn moves and breathes after the shard is obtained");
        w=chapter(3);w.belfry.fireMask=7;w.belfry.guardianDefeated=true;w.belfry.respawn(w);w.belfry.wakeTime=0;
        RenderTest.black(ArenaArtTest.pixels(frame(art,w,"belfry-respawn-black")),"Chapter wake fade");w.belfry.wakeTime=1250;frame(art,w,"belfry-respawn-waking");
        w=new World();w.enterCamp(true);w.campTime=1800;frame(art,w,"belfry-camp-exit");
        check(true,"Chapter entrance, phase change, ending and checkpoint waking render through real MIDP graphics");
        System.out.println("ALL "+checks+" BELFRY RENDER CHECKS PASSED; "+frames+" chapter frames exported");System.exit(0);
    }
    public static void main(String[] args){try{runTests();}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
