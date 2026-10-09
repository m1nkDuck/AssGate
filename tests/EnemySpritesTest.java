import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.util.Arrays;
import java.lang.reflect.Field;

/** Production enemy poses, lighting, anchored collapse and visual-only model clocks. */
public final class EnemySpritesTest {
    static final int BACKGROUND=0x152331;
    static int checks;
    static void check(boolean value,String message){if(!value)throw new RuntimeException(message);checks++;System.out.println("PASS: "+message);}
    static int[] pixels(Image image){int[] p=new int[image.getWidth()*image.getHeight()];image.getRGB(p,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());return p;}
    static int[] tile(Image atlas,int column,int row){int[] p=new int[128*80];atlas.getRGB(p,0,128,column*128,row*80,128,80);return p;}
    static void direction(Belfry.Enemy e,int row){e.dx=row==1?-1:row==2?1:0;e.dy=row==0?1:row==3?-1:0;}
    static void pose(Belfry.Enemy e,int column){
        e.hp=48;e.active=true;e.hitTime=0;e.state=column==5?World.WARNING:column==6?World.ACTIVE:World.SEEK;
        e.moving=column>=1&&column<=4;e.walkTime=e.moving?(column-1)*120:0;e.deathTime=0;
        if(column==7){e.hp=0;e.active=false;e.moving=false;e.deathTime=Belfry.ENEMY_DEATH_TIME;}
    }
    static int[] render(EnemySprites sprites,Belfry.Enemy e,int background){
        Image image=Image.createImage(128,80);Graphics g=image.getGraphics();g.setColor(background);g.fillRect(0,0,128,80);
        e.x=64;e.y=68;if(!sprites.draw(g,e,1700))throw new RuntimeException("Production enemy atlas did not load");return pixels(image);
    }
    static Image[] images(EnemySprites sprites,String name)throws Exception{Field f=EnemySprites.class.getDeclaredField(name);f.setAccessible(true);return (Image[])f.get(sprites);}
    static void assets()throws Exception{
        EnemySprites sprites=new EnemySprites();Belfry.Enemy e=new Belfry.Enemy();
        String[] files={"/belfry-guard-atlas.png","/belfry-spearman-atlas.png"};
        for(int kind=0;kind<2;kind++){
            Image atlas=Image.createImage(EnemySpritesTest.class.getResourceAsStream(files[kind]));
            check(atlas.getWidth()==1024&&atlas.getHeight()==320,"Enemy type "+kind+" keeps 32 independent 128x80 weapon/body cells");
            e.elite=kind==1;
            for(int row=0;row<4;row++){
                long[] walkHashes=new long[4];direction(e,row);
                for(int column=0;column<8;column++){
                    int[] raw=tile(atlas,column,row);int area=0,top=80,bottom=-1,left=128,right=-1,blade=0;
                    for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                        int a=raw[y*128+x]>>>24;if(a!=0&&a!=255)throw new RuntimeException("Enemy alpha is not binary");
                        if(a==255){
                            area++;top=Math.min(top,y);bottom=Math.max(bottom,y);left=Math.min(left,x);right=Math.max(right,x);
                            if(x==0||x==127||y==0||y==79)throw new RuntimeException("Enemy body/weapon clips a cell edge: "+kind+"/"+row+"/"+column);
                            int r=(raw[y*128+x]>>>16)&255,g=(raw[y*128+x]>>>8)&255,b=raw[y*128+x]&255;
                            if(y>=44&&y<=68&&Math.abs(x-64)>11&&Math.min(r,Math.min(g,b))>=130&&Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))<=80)blade++;
                        }
                    }
                    if(area<160||area>2400||bottom!=68)throw new RuntimeException("Missing body, opaque backdrop or wandering ground: "+kind+"/"+row+"/"+column+" area="+area+" bottom="+bottom);
                    if(column==0&&Math.abs(bottom-top+1-(kind==0?44:48))>1)throw new RuntimeException("Enemy standing size differs from the 44/48px contract");
                    if(column<=4&&kind==0&&blade<8)throw new RuntimeException("Guard sword vanishes beside the torso: "+row+"/"+column);
                    if(column<=4&&kind==1&&right-left+1<48)throw new RuntimeException("Spearman loses the pole extending beyond his body: "+row+"/"+column);
                    pose(e,column);e.lit=true;int[] lit=render(sprites,e,BACKGROUND),contrast=render(sprites,e,0xe0c090);
                    e.lit=false;int[] dark=render(sprites,e,BACKGROUND),darkContrast=render(sprites,e,0xe0c090);
                    int changed=0;
                    for(int i=0;i<raw.length;i++){
                        if((raw[i]>>>24)==0){
                            if((lit[i]&0xffffff)!=BACKGROUND||(contrast[i]&0xffffff)!=0xe0c090||(dark[i]&0xffffff)!=BACKGROUND||(darkContrast[i]&0xffffff)!=0xe0c090)throw new RuntimeException("Enemy matte changes between contrasting floors");
                        }else{
                            if((lit[i]&0xffffff)!=(raw[i]&0xffffff)||(contrast[i]&0xffffff)!=(raw[i]&0xffffff))throw new RuntimeException("Renderer selects a wrong enemy pose/direction/anchor");
                            if((dark[i]&0xffffff)!=(darkContrast[i]&0xffffff))throw new RuntimeException("Dark armour loses an opaque body or weapon pixel");
                            for(int shift=0;shift<=16;shift+=8)if(((dark[i]>>>shift)&255)>((lit[i]>>>shift)&255))throw new RuntimeException("Darkness brightens an enemy channel");
                            if(dark[i]!=lit[i])changed++;
                        }
                    }
                    if(changed<area*3/4)throw new RuntimeException("Enemy remains bright outside torch light");
                    if(column>=1&&column<=4){long hash=1;for(int p:raw)hash=hash*31+p;walkHashes[column-1]=hash;}
                }
                for(int a=0;a<4;a++)for(int b=a+1;b<4;b++)if(walkHashes[a]==walkHashes[b])throw new RuntimeException("Enemy walk repeats a static pose: "+kind+"/"+row);
            }
            check(true,"All 32 poses of type "+kind+" preserve alpha, direction, planted ground, full weapon and the same silhouette in darkness");
        }
        Image[] original=images(sprites,"atlas"),dark=images(sprites,"dark");Image first=original[0],firstDark=dark[0];
        for(int kind=0;kind<2;kind++){
            int[] a=pixels(original[kind]),b=pixels(dark[kind]);for(int i=0;i<a.length;i++)if((a[i]>>>24)!=(b[i]>>>24))throw new RuntimeException("Dark atlas changes source alpha");
        }
        e.elite=false;pose(e,0);e.lit=false;render(sprites,e,BACKGROUND);e.lit=true;render(sprites,e,BACKGROUND);
        check(first==images(sprites,"atlas")[0]&&firstDark==images(sprites,"dark")[0],"Lit/dark atlases are reused without rebuilding pixels for each frame");
        EnemySprites absent=new EnemySprites();Field loaded=EnemySprites.class.getDeclaredField("loaded");loaded.setAccessible(true);Arrays.fill((boolean[])loaded.get(absent),true);
        check(!absent.draw(Image.createImage(128,80).getGraphics(),e,0),"Missing atlases return control to the native guard fallback");
    }
    static World chapter(){World w=new World();w.oathkeeperComplete=true;w.belfry.enterRoom(w,1,true);w.belfry.entranceQuoteTime=0;return w;}
    static void model(){
        World w=chapter();Belfry.Enemy e=w.belfry.enemies[0];w.px=40;w.py=145;
        float x=e.x,y=e.y;w.update(100,0,0,false,false,false);
        check(e.moving&&e.walkTime==100&&(e.x!=x||e.y!=y),"Walk time advances only when guard movement actually changes its position");
        w.px=e.x-20;w.py=e.y;e.time=0;x=e.x;y=e.y;w.update(100,0,0,false,false,false);
        check(e.state==World.SEEK&&!e.moving&&e.walkTime==100&&e.x==x&&e.y==y,"A seeking guard standing at its stop radius uses idle instead of moonwalking");
        e.state=World.WARNING;e.time=0;w.update(100,0,0,false,false,false);
        check(!e.moving&&e.walkTime==100&&e.x==x&&e.y==y,"Warning locks the enemy position and walk clock");
        e.state=World.RECOVER;e.time=0;w.update(100,0,0,false,false,false);
        check(!e.moving&&e.walkTime==100,"Recovery does not consume locomotion frames");
        e.state=World.SEEK;e.time=123;e.dx=0;e.dy=1;w.px=e.x;w.py=e.y-26;w.fx=0;w.fy=1;
        int hp=e.hp,state=e.state,time=e.time;w.belfry.strike(w);
        check(e.hp==hp-Balance.SWORD_DAMAGE&&e.hitTime==Belfry.ENEMY_HIT_TIME&&e.x==x&&e.y==y&&e.state==state&&e.time==time,"A real sword hit starts visual recoil without changing enemy position or attack state");
        w.px=40;w.py=200;w.update(179,0,0,false,false,false);check(e.hitTime==1,"Hit feedback retains its final millisecond");
        w.update(1,0,0,false,false,false);check(e.hitTime==0,"Hit feedback expires after 180ms");
        w.px=e.x;w.py=e.y-26;w.fx=0;w.fy=1;w.belfry.strike(w);x=e.x;y=e.y;
        check(!e.active&&e.hp==0&&!e.moving&&e.deathTime==0&&!w.belfry.hazardAt(e.x,e.y),"The lethal hit clears combat and hazards immediately while starting a visual corpse at time zero");
        w.px=40;w.py=200;w.update(719,0,0,false,false,false);
        check(e.deathTime==719&&e.x==x&&e.y==y&&!e.active,"The corpse remains anchored independently of the next guard's spawn");
        w.update(1,0,0,false,false,false);check(e.deathTime==Belfry.ENEMY_CORPSE_TIME,"Corpse visibility ends at the exact 720ms boundary");
        w.belfry.enterRoom(w,2,true);
        for(int i=0;i<2;i++){e=w.belfry.enemies[i];if(e.moving||e.walkTime!=0||e.hitTime!=0||e.deathTime<=Belfry.ENEMY_CORPSE_TIME)throw new RuntimeException("Room change leaves an old corpse or visual timer");}
        check(true,"Room transitions clear unused enemies, old corpses and animation clocks");
        Game g=new Game(null,false);g.world.soundOn=false;g.world.oathkeeperComplete=true;g.world.belfry.enterRoom(g.world,1,true);
        e=g.world.belfry.enemies[0];e.hp=0;e.active=false;e.deathTime=200;e.hitTime=100;
        g.keyPressed('p');g.tick();g.keyReleased('p');int clock=g.world.clock;
        for(int i=0;i<20;i++)g.tick();check(e.deathTime==200&&e.hitTime==100&&g.world.clock==clock,"Pausing the real Canvas freezes enemy hit and collapse clocks");g.stop();
    }
    static void effects()throws Exception{
        EnemySprites sprites=new EnemySprites();Belfry.Enemy e=new Belfry.Enemy();direction(e,0);pose(e,0);e.lit=true;
        int[] idle=render(sprites,e,BACKGROUND);e.hitTime=135;int[] hurt=render(sprites,e,BACKGROUND);
        check(!Arrays.equals(idle,hurt),"A lit enemy visibly recoils and flashes when hit");e.hitTime=0;
        check(Arrays.equals(idle,render(sprites,e,BACKGROUND)),"Expired hit feedback restores the exact unhurt pose");
        e.hp=0;e.active=false;int[][] stages=new int[3][];int[] ages={0,200,500};
        for(int i=0;i<ages.length;i++){
            e.deathTime=ages[i];stages[i]=render(sprites,e,BACKGROUND);int visible=0;
            for(int y=0;y<80;y++)for(int x=0;x<128;x++)if((stages[i][y*128+x]&0xffffff)!=BACKGROUND){visible++;if(y>68)throw new RuntimeException("Collapse passes through the planted floor");}
            if(visible<120)throw new RuntimeException("Collapse pose disappears before its final fallen frame");
        }
        check(!Arrays.equals(stages[0],stages[1])&&!Arrays.equals(stages[1],stages[2]),"Death visibly progresses through recoil, lowered attack and settled fallen poses");
        e.deathTime=719;check(Arrays.equals(stages[2],render(sprites,e,BACKGROUND)),"The settled fallen pose holds without flicker until the hide boundary");
        e.deathTime=720;int[] gone=render(sprites,e,BACKGROUND);for(int p:gone)if((p&0xffffff)!=BACKGROUND)throw new RuntimeException("Expired corpse still draws pixels");
        check(true,"An expired corpse draws no pixels at 720ms");
        pose(e,3);e.lit=false;e.x=64;e.y=68;int walk=e.walkTime,hit=e.hitTime,death=e.deathTime;boolean moving=e.moving;
        int[] first=render(sprites,e,BACKGROUND);for(int i=0;i<8;i++)if(!Arrays.equals(first,render(sprites,e,BACKGROUND)))throw new RuntimeException("Repeated rendering advances an enemy pose");
        check(e.walkTime==walk&&e.hitTime==hit&&e.deathTime==death&&e.moving==moving,"Drawing is stable at a fixed tick and cannot mutate model clocks");
        for(int kind=0;kind<2;kind++)for(int column=0;column<8;column++){
            Image image=Image.createImage(320,240);Graphics g=image.getGraphics();g.setColor(BACKGROUND);g.fillRect(0,0,320,240);
            Art.text(g,kind==0?"GUARD":"SPEARMAN",8,8,0xe1c5a7,2);
            for(int row=0;row<4;row++){
                pose(e,column);e.elite=kind==1;e.lit=true;direction(e,row);e.x=row%2==0?80:240;e.y=row<2?100:205;sprites.draw(g,e,1700);
                Art.text(g,new String[]{"DOWN","LEFT","RIGHT","UP"}[row],(int)e.x-12,(int)e.y+9,0xb9c3cf,1);
            }
            ArenaArtTest.save(image,(kind==0?"belfry-guard-animation-":"belfry-spearman-animation-")+column);
        }
        check(true,"Exported all eight production poses in all four directions for both enemy types");
    }
    static void runTests()throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,EnemySpritesTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        model();assets();effects();System.out.println("ALL "+checks+" ENEMY ANIMATION CHECKS PASSED");System.exit(0);
    }
    public static void main(String[] args){try{runTests();}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
