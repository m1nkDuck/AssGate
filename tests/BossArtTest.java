import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
/** Checks the production sprite atlas, its ground anchor, and native fallback. */
public class BossArtTest {
    static final int WIDTH=240,HEIGHT=220,X=120,GROUND=170,BACKGROUND=0x061119;
    static final int[] DX={0,0,-1,1},DY={-1,1,0,0};
    static final String[] DIRECTIONS={"UP","DOWN","LEFT","RIGHT"};
    static void check(boolean condition,String message){if(!condition)throw new RuntimeException(message);}
    static Image blank(){
        Image image=Image.createImage(WIDTH,HEIGHT);Graphics g=image.getGraphics();
        g.setColor(BACKGROUND);g.fillRect(0,0,WIDTH,HEIGHT);return image;
    }
    static int[] pixels(Image image){
        int[] values=new int[image.getWidth()*image.getHeight()];
        image.getRGB(values,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());return values;
    }
    static int[] bounds(Image image){
        int[] values=pixels(image),result={WIDTH,HEIGHT,-1,-1};
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++)if((values[y*WIDTH+x]&0xffffff)!=BACKGROUND){
            result[0]=Math.min(result[0],x);result[1]=Math.min(result[1],y);
            result[2]=Math.max(result[2],x);result[3]=Math.max(result[3],y);
        }
        check(result[2]>=result[0],"Empty boss drawing");return result;
    }
    static Image body(int dir,int step,boolean walking,int pose,int time,int phase){
        Image image=blank();
        KnightArt.bossBody(image.getGraphics(),X,GROUND,dir,step,walking,step*110,pose,time,phase);return image;
    }
    static long hash(Image image,int x,int y,int width,int height){
        return DirectionTest.hash(image,x,y,width,height);
    }
    static boolean bone(int color){
        int r=color>>16&255,g=color>>8&255,b=color&255;
        return r>=180&&g>=155&&b>=100&&r>=g&&g>=b&&r-g<=60&&g-b<=70;
    }
    static int bones(Image image,int top,int bottom){
        int[] values=pixels(image);int count=0;
        for(int y=top;y<=bottom;y++)for(int x=0;x<WIDTH;x++)if(bone(values[y*WIDTH+x]))count++;
        return count;
    }
    static void visibleSkeleton(Image image,int dir,String pose){
        int[] box=bounds(image);
        check(bones(image,box[1],Math.min(HEIGHT-1,box[1]+27))>=12,pose+" has no readable skull in "+DIRECTIONS[dir]);
        check(bones(image,GROUND-56,GROUND-15)>=8,pose+" hides all exposed body bones in "+DIRECTIONS[dir]);
    }
    static void ground(Image image,String name){
        int[] box=bounds(image);
        check(box[0]>0&&box[1]>0&&box[2]<WIDTH-1&&box[3]<HEIGHT-1,name+" is clipped");
        check(Math.abs(box[3]-GROUND)<=1,name+" moves planted soles away from the ground: "+box[3]);
    }
    static void handTouchesBody(Image image,int dir,int pose,int time){
        handTouchesBody(image,dir,pose,time,0,false);
    }
    static void handTouchesBody(Image image,int dir,int pose,int time,int step,boolean walking){
        int hx=SkeletonBossArt.handX(X,dir,pose,time,step,walking),hy=SkeletonBossArt.handY(GROUND,dir,pose,time,step,walking);
        int[] values=pixels(image);int count=0;
        for(int y=hy-2;y<=hy+2;y++)for(int x=hx-2;x<=hx+2;x++)
            if(x>=0&&x<WIDTH&&y>=0&&y<HEIGHT&&(values[y*WIDTH+x]&0xffffff)!=BACKGROUND)count++;
        check(count>=4,"Sword grip detached from the drawn hand in "+DIRECTIONS[dir]+" pose "+pose);
    }
    static Image integrated(Art art,int dir,int state,int events,int phase){
        return integrated(art,dir,state,events,phase,0,false);
    }
    static Image integrated(Art art,int dir,int state,int events,int phase,int step,boolean walking){
        Image image=blank();World w=new World();w.mode=World.FIGHT;
        w.bx=X;w.by=GROUND;w.px=X;w.py=GROUND+(walking?80:30);w.bdx=DX[dir];w.bdy=DY[dir];w.clock=step*110;
        w.bstate=state;w.events=events;w.phase=phase;w.attack=1;art.boss(image.getGraphics(),w);return image;
    }
    static void saveRow(String name,Image[] frames,String[] labels)throws Exception{
        Image image=Image.createImage(frames.length*WIDTH,200);Graphics g=image.getGraphics();
        g.setColor(BACKGROUND);g.fillRect(0,0,image.getWidth(),image.getHeight());
        for(int i=0;i<frames.length;i++){
            g.drawRegion(frames[i],0,0,WIDTH,200,0,i*WIDTH,0,Graphics.TOP|Graphics.LEFT);
            Art.text(g,labels[i],i*WIDTH+8,186,0xc7b6a3,1);
        }
        int[] values=pixels(image);BufferedImage out=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
        out.setRGB(0,0,image.getWidth(),image.getHeight(),values,0,image.getWidth());ImageIO.write(out,"png",new File("preview/"+name+".png"));
    }
    static void northBoundary(){
        check(new World().by>=Balance.BOSS_MIN_Y,"New encounter spawns the skeleton above its visible arena boundary");
        int[] offsets={-50,0,50};
        for(int phase=1;phase<=2;phase++)for(int i=0;i<offsets.length;i++){
            World seek=new World();seek.mode=World.FIGHT;seek.phase=phase;
            seek.bx=160;seek.by=Balance.BOSS_MIN_Y+0.1f;seek.px=160+offsets[i];seek.py=57;
            seek.bstate=World.SEEK;seek.bt=0;seek.update(33,0,0,false,false,false);
            check(seek.by>=Balance.BOSS_MIN_Y,"Northward seeking cuts the skeleton into the HUD in phase "+phase);
            World charge=new World();charge.mode=World.FIGHT;charge.phase=phase;
            charge.bx=160;charge.by=Balance.BOSS_MIN_Y+16;charge.px=160+offsets[i];charge.py=57;
            charge.attack=2;charge.lockAttack();check(charge.chargeLength>0,"Northward charge was disabled rather than bounded");
            charge.bstate=World.ACTIVE;charge.bt=0;charge.update(Balance.ACTIVE[2],0,0,false,false,false);
            check(charge.by>=Balance.BOSS_MIN_Y-0.001f&&charge.by<=Balance.BOSS_MIN_Y+16,"Northward charge crosses the visible skeleton boundary in phase "+phase);
        }
    }
    static Image sprite(BossSprites sprites,int dir,int state,int step,boolean walking,int events,int phase,boolean fallen){
        Image image=blank();World w=new World();w.mode=fallen?World.WIN:World.FIGHT;
        w.bx=X;w.by=GROUND;w.bstate=state;w.events=events;w.phase=phase;
        check(sprites.draw(image.getGraphics(),w,dir,step,walking),"Production boss atlas did not load");return image;
    }
    static void sourcePose(Image atlas,Image rendered,int row,int column,String name){
        Image expected=blank();expected.getGraphics().drawRegion(atlas,column*224,row*112,224,112,0,X-112,GROUND-100,Graphics.TOP|Graphics.LEFT);
        check(hash(expected,0,0,WIDTH,HEIGHT)==hash(rendered,0,0,WIDTH,HEIGHT),name+" selects the wrong direction or pose");
    }
    static boolean swordPresent(int[] raw,int atlasWidth,int row,int column){
        boolean[] steel=new boolean[224*112];int[] queue=new int[steel.length];
        for(int y=0;y<112;y++)for(int x=0;x<224;x++){
            int color=raw[(row*112+y)*atlasWidth+column*224+x];
            int r=color>>16&255,g=color>>8&255,b=color&255;
            steel[y*224+x]=(color>>>24)==255&&Math.min(r,Math.min(g,b))>=120&&b>=r&&Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))<=60;
        }
        for(int start=0;start<steel.length;start++)if(steel[start]){
            int first=0,last=1,minX=224,minY=112,maxX=-1,maxY=-1;queue[0]=start;steel[start]=false;
            while(first<last){
                int point=queue[first++],x=point%224,y=point/224;
                minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);
                for(int yy=Math.max(0,y-1);yy<=Math.min(111,y+1);yy++)for(int xx=Math.max(0,x-1);xx<=Math.min(223,x+1);xx++){
                    int next=yy*224+xx;if(steel[next]){steel[next]=false;queue[last++]=next;}
                }
            }
            if(last>=25&&Math.max(maxX-minX+1,maxY-minY+1)>=14)return true;
        }
        return false;
    }
    static void production(Art art,int heroHeight)throws Exception{
        Image atlas=Image.createImage(BossArtTest.class.getResourceAsStream("/boss-atlas.png"));
        check(atlas.getWidth()==1792&&atlas.getHeight()==448,"Expected 32 complete boss cells, 224x112 each");
        int[] raw=pixels(atlas);int aw=atlas.getWidth();
        for(int row=0;row<4;row++)for(int col=0;col<8;col++){
            int solid=0,ivory=0,lowest=-1;
            for(int y=0;y<112;y++)for(int x=0;x<224;x++){
                int color=raw[(row*112+y)*aw+col*224+x],alpha=color>>>24;
                check(alpha==0||alpha==255,"Non-binary MIDP alpha in boss cell "+row+","+col);
                if(alpha==255){
                    solid++;lowest=Math.max(lowest,y);if(bone(color))ivory++;
                    check(x>0&&x<223&&y>0&&y<111,"Boss body or sword is clipped at a cell border: "+row+","+col);
                }
            }
            check(solid>=500&&ivory>=40,"Empty pose or missing visible skeleton: "+row+","+col);
            check(lowest==100,"Boss cell moves its soles away from the shared ground: "+row+","+col);
            check(swordPresent(raw,aw,row,col),"Sword disappears in boss pose "+row+","+col);
        }
        BossSprites sprites=new BossSprites();Image[] idle=new Image[4];long[] facings=new long[4];int[] rows={3,0,1,2};
        for(int dir=0;dir<4;dir++){
            idle[dir]=sprite(sprites,dir,World.RECOVER,0,false,0,1,false);int[] box=bounds(idle[dir]);
            int height=box[3]-box[1]+1;check(height==88&&height>=heroHeight*1.9&&height<=heroHeight*2.1,"Production boss is not twice the hero height in "+DIRECTIONS[dir]);
            ground(idle[dir],"Production idle "+DIRECTIONS[dir]);visibleSkeleton(idle[dir],dir,"Production idle");
            sourcePose(atlas,idle[dir],rows[dir],0,"Idle "+DIRECTIONS[dir]);
            facings[dir]=hash(idle[dir],0,0,WIDTH,HEIGHT);long[] feet=new long[4];
            for(int frame=0;frame<4;frame++){
                Image walk=sprite(sprites,dir,World.SEEK,frame*2,true,0,1,false);ground(walk,"Production walk "+DIRECTIONS[dir]);
                sourcePose(atlas,walk,rows[dir],frame+1,"Walk "+DIRECTIONS[dir]+" frame "+frame);
                int[] walkBox=bounds(walk);check(walkBox[3]-walkBox[1]+1>=86&&walkBox[3]-walkBox[1]+1<=93,"Walk changes skeleton proportions in "+DIRECTIONS[dir]);
                visibleSkeleton(walk,dir,"Production walk");feet[frame]=hash(walk,X-45,GROUND-37,90,40);
            }
            int unique=0;for(int f=0;f<4;f++){boolean fresh=true;for(int j=0;j<f;j++)if(feet[f]==feet[j])fresh=false;if(fresh)unique++;}
            check(unique>=3,"Production walk does not alternate feet in "+DIRECTIONS[dir]);
            Image windup=sprite(sprites,dir,World.WARNING,0,false,0,1,false),strike=sprite(sprites,dir,World.ACTIVE,0,false,0,1,false);
            sourcePose(atlas,windup,rows[dir],5,"Windup "+DIRECTIONS[dir]);sourcePose(atlas,strike,rows[dir],6,"Strike "+DIRECTIONS[dir]);
            check(hash(windup,0,0,WIDTH,HEIGHT)!=hash(strike,0,0,WIDTH,HEIGHT),"Production sword never swings in "+DIRECTIONS[dir]);
            ground(windup,"Production windup "+DIRECTIONS[dir]);ground(strike,"Production strike "+DIRECTIONS[dir]);
            visibleSkeleton(windup,dir,"Production windup");visibleSkeleton(strike,dir,"Production strike");
            Image hurt=sprite(sprites,dir,World.RECOVER,0,false,World.BOSS_HIT,1,false),phase2=sprite(sprites,dir,World.RECOVER,0,false,0,2,false);
            int[] hurtBox=bounds(hurt);check(Math.abs(hurtBox[0]-box[0])==2&&hurtBox[1]==box[1]&&hurtBox[3]==box[3],"Production hit recoil shifts the ground or loses the skull");
            check(hash(phase2,0,0,WIDTH,HEIGHT)!=facings[dir],"Production phase II has no visible cue");
            check(hash(phase2,0,0,WIDTH,GROUND-6)==hash(idle[dir],0,0,WIDTH,GROUND-6),"Phase II recolors or displaces the skeleton instead of retaining readable bones");
            Image fallen=sprite(sprites,dir,World.RECOVER,0,false,0,1,true);sourcePose(atlas,fallen,rows[dir],7,"Victory "+DIRECTIONS[dir]);
            int[] corpse=bounds(fallen);check(corpse[3]==GROUND&&corpse[3]-corpse[1]+1<=40&&corpse[2]-corpse[0]+1>=80,"Victory skeleton does not lie on the ground");
            Image integrated=integrated(art,dir,World.RECOVER,0,1),hit=integrated(art,dir,World.RECOVER,World.BOSS_HIT,1);
            check(hash(integrated,X-45,GROUND-94,90,60)==hash(idle[dir],X-45,GROUND-94,90,60),"Art.boss uses the native fallback despite a complete sprite atlas");
            check(hash(integrated,X-45,GROUND-94,90,38)!=hash(hit,X-45,GROUND-94,90,38),"Art.boss misses production hit recoil");
        }
        for(int a=0;a<4;a++)for(int b=a+1;b<4;b++)check(facings[a]!=facings[b],"Production repeats a facing");
        saveRow("boss-skeleton-directions",idle,DIRECTIONS);
        saveRow("boss-skeleton-actions",new Image[]{idle[1],sprite(sprites,1,World.SEEK,2,true,0,1,false),sprite(sprites,1,World.WARNING,0,false,0,1,false),sprite(sprites,1,World.ACTIVE,0,false,0,1,false),sprite(sprites,1,World.RECOVER,0,false,World.BOSS_HIT,1,false),sprite(sprites,1,World.RECOVER,0,false,0,2,false),sprite(sprites,1,World.RECOVER,0,false,0,1,true)},new String[]{"IDLE","WALK","WINDUP","STRIKE","HURT","PHASE II","FALLEN"});
        System.out.println("PASS: Production boss has 32 complete binary-alpha poses, 88px idle height, exposed bones and four distinct facings.");
        System.out.println("PASS: Production walking, sword windup/strike, hit recoil, phase II and victory select the atlas and preserve ground anchors.");
    }
    static void runTests()throws Exception{
        RenderTest context=new RenderTest();
        DeviceImpl device=DeviceImpl.create(context,BossArtTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);
        DeviceFactory.setDevice(device);device.init();new File("preview").mkdirs();
        Art art=new Art();Image[] idle=new Image[4];long[] directions=new long[4];
        for(int dir=0;dir<4;dir++){
            idle[dir]=body(dir,0,false,World.IDLE,0,1);int[] box=bounds(idle[dir]);
            int height=box[3]-box[1]+1;
            check(height>=87&&height<=89,"Boss head-to-sole height must be 88px in "+DIRECTIONS[dir]+", got "+height);
            ground(idle[dir],"Idle "+DIRECTIONS[dir]);visibleSkeleton(idle[dir],dir,"Idle");
            handTouchesBody(idle[dir],dir,World.IDLE,0);
            directions[dir]=hash(idle[dir],0,0,WIDTH,HEIGHT);
            long[] lowerFrames=new long[8];int distinct=0;
            for(int step=0;step<8;step++){
                Image walk=body(dir,step,true,World.IDLE,0,1);ground(walk,"Walk "+DIRECTIONS[dir]+" frame "+step);
                handTouchesBody(walk,dir,World.IDLE,0,step,true);
                visibleSkeleton(walk,dir,"Walk");lowerFrames[step]=hash(walk,X-45,GROUND-37,90,40);
                boolean fresh=true;for(int old=0;old<step;old++)if(lowerFrames[old]==lowerFrames[step])fresh=false;
                if(fresh)distinct++;
            }
            check(distinct>=3,"Walk feet do not articulate in "+DIRECTIONS[dir]);
            Image windup=body(dir,0,false,World.ATTACK,0,1),strike=body(dir,0,false,World.ATTACK,200,1);
            check(hash(windup,0,0,WIDTH,HEIGHT)!=hash(strike,0,0,WIDTH,HEIGHT),"Attack arm does not move in "+DIRECTIONS[dir]);
            for(int time=0;time<=200;time+=200){
                Image attack=time==0?windup:strike;ground(attack,"Attack "+DIRECTIONS[dir]);
                visibleSkeleton(attack,dir,"Attack");handTouchesBody(attack,dir,World.ATTACK,time);
            }
            Image hurt=body(dir,0,false,World.HURT,0,1),phase2=body(dir,0,false,World.IDLE,0,2);
            check(hash(hurt,0,0,WIDTH,HEIGHT)!=directions[dir],"Hurt has no recoil in "+DIRECTIONS[dir]);
            check(hash(phase2,0,0,WIDTH,HEIGHT)!=directions[dir],"Phase II has no visible change in "+DIRECTIONS[dir]);
            ground(hurt,"Hurt "+DIRECTIONS[dir]);ground(phase2,"Phase II "+DIRECTIONS[dir]);
            visibleSkeleton(hurt,dir,"Hurt");visibleSkeleton(phase2,dir,"Phase II");
        }
        for(int a=0;a<4;a++)for(int b=a+1;b<4;b++)check(directions[a]!=directions[b],"Duplicate boss directions");
        Image hero=blank();World heroWorld=new World();heroWorld.mode=World.FIGHT;
        heroWorld.px=X;heroWorld.py=GROUND;heroWorld.pstate=World.IDLE;heroWorld.moving=false;
        new HeroSprites().draw(hero.getGraphics(),heroWorld,1);
        int[] heroBox=bounds(hero);int heroHeight=heroBox[3]-heroBox[1]+1;
        check(heroHeight>=43&&heroHeight<=45,"Hero reference changed from 44px: "+heroHeight);
        check((bounds(idle[1])[3]-bounds(idle[1])[1]+1)>=heroHeight*1.9,"Boss is not twice the hero's visible height");
        int[] arenaY={(int)new World().by,Balance.BOSS_MIN_Y};
        for(int i=0;i<arenaY.length;i++){
            Image atNorth=blank();KnightArt.bossBody(atNorth.getGraphics(),X,arenaY[i],1,0,false,0,World.IDLE,0,1);
            int[] north=bounds(atNorth);
            check(north[1]>=24&&north[3]-north[1]+1==88,"Spawn or north arena edge cuts the boss skull into the HUD");
        }
        northBoundary();
        Image fallen=blank();KnightArt.fallen(fallen.getGraphics(),X,GROUND,true);int[] corpse=bounds(fallen);
        check(corpse[3]-corpse[1]+1<30&&corpse[2]-corpse[0]+1>45,"Victory keeps a standing boss instead of fallen skeleton");
        check(corpse[3]>=GROUND-2&&corpse[3]<=GROUND+8,"Fallen skeleton detached from ground");
        check(bones(fallen,corpse[1],corpse[3])>=12,"Fallen skeleton lost all readable bones");
        saveRow("boss-skeleton-fallback-directions",idle,DIRECTIONS);
        saveRow("boss-skeleton-fallback-actions",new Image[]{idle[1],body(1,2,true,World.IDLE,0,1),body(1,0,false,World.ATTACK,200,1),body(1,0,false,World.HURT,0,1),body(1,0,false,World.IDLE,0,2),fallen},new String[]{"IDLE","WALK","ATTACK","HURT","PHASE II","FALLEN"});
        System.out.println("PASS: Native skeleton fallback is 88px versus 44px hero; exposed bones in four distinct directions.");
        System.out.println("PASS: Native walk, attack and hurt keep planted soles and attached hands; phase II and fallen remain readable.");
        System.out.println("PASS: Spawn, seeking and northward charges preserve the visible boss boundary in both phases.");
        production(art,heroHeight);
        System.exit(0);
    }
    public static void main(String[] args){
        try{runTests();}catch(Throwable error){error.printStackTrace();System.exit(1);}
    }
}
