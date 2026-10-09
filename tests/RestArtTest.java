import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;

/** Validate the added camp atlas, transitions and actual MIDP composition. */
public final class RestArtTest {
    static void check(boolean b,String message){if(!b)throw new RuntimeException(message);}
    static int[] tile(Image atlas,int col,int row){int[] p=new int[80*64];atlas.getRGB(p,0,80,col*80,row*64,80,64);return p;}
    static int[] render(HeroSprites sprites,World w,int dir,int background){
        Image cell=Image.createImage(80,64);Graphics g=cell.getGraphics();g.setColor(background);g.fillRect(0,0,80,64);
        w.px=40;w.py=52;sprites.draw(g,w,dir);int[] pixels=new int[80*64];cell.getRGB(pixels,0,80,0,0,80,64);return pixels;
    }
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,RestArtTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        Image atlas=Image.createImage(RestArtTest.class.getResourceAsStream("/hero-rest-atlas.png"));
        Image original=Image.createImage(RestArtTest.class.getResourceAsStream("/hero-atlas.png"));
        check(atlas.getWidth()==480&&atlas.getHeight()==256,"Rest atlas must keep 24 independent 80x64 cells");
        HeroSprites sprites=new HeroSprites();World w=new World();w.enterCamp(false);w.campTime=Balance.CAMP_WAKE_TIME;int[] directions={1,2,3,0};
        for(int row=0;row<4;row++){
            check(java.util.Arrays.equals(tile(atlas,0,row),tile(original,0,row)),"Standing rest pose must exactly match the existing hero, direction "+row);
            for(int col=0;col<6;col++){
                int[] pixels=tile(atlas,col,row);int count=0,top=64,bottom=-1;
                for(int y=0;y<64;y++)for(int x=0;x<80;x++){
                    int alpha=pixels[y*80+x]>>>24;check(alpha==0||alpha==255,"Rest transparency must be binary");
                    if(alpha==255){count++;top=Math.min(top,y);bottom=Math.max(bottom,y);check(x>0&&x<79&&y>0&&y<63,"Rest cell clips body or sword");}
                }
                check(count>250&&count<1200,"Rest sprite has missing body or an opaque backdrop");
                check(bottom==52,"Rest ground anchor moves while sitting");
                if(col==5)check(bottom-top+1>=22&&bottom-top+1<=30,"Final pose must visibly sit lower than the 44px standing hero");
                w.restState=World.REST_SIT;w.restTime=col*Balance.REST_SIT_TIME/6;
                int[] dark=render(sprites,w,directions[row],0x18212a),light=render(sprites,w,directions[row],0xe0c090);
                for(int i=0;i<pixels.length;i++){
                    if((pixels[i]>>>24)==0)check((dark[i]&0xffffff)==0x18212a&&(light[i]&0xffffff)==0xe0c090,"Transparent rest backdrop changes on contrasting floors");
                    else check((dark[i]&0xffffff)==(pixels[i]&0xffffff),"Hero rest draw selected a wrong frame or anchor");
                }
            }
        }
        for(int time=0;time<Balance.REST_SIT_TIME;time++)check(RestSprites.frame(World.REST_SIT,time)==RestSprites.frame(World.REST_RISE,Balance.REST_RISE_TIME-time),"Early cancellation changes the hero pose");
        w.enterCamp(false);w.campTime=Balance.CAMP_WAKE_TIME;w.restState=World.REST_IDLE;w.restTime=0;
        int[] sit=render(sprites,w,2,0x18212a);w.restTime=700;check(!java.util.Arrays.equals(sit,render(sprites,w,2,0x18212a)),"Resting should visibly breathe");
        w.restTime=1400;check(java.util.Arrays.equals(sit,render(sprites,w,2,0x18212a)),"Resting breath loop must repeat without drift");
        w.mode=World.FIGHT;w.restState=World.REST_IDLE;w.moving=false;w.clock=0;
        int[] combat=render(sprites,w,2,0x18212a);w.restState=World.REST_NONE;
        check(java.util.Arrays.equals(combat,render(sprites,w,2,0x18212a)),"Camp rest state must not replace combat poses");
        Art art=new Art();int[] xs={148,190,106,148},ys={113,164,151,193};
        for(int dir=0;dir<4;dir++){
            w.enterCamp(false);w.campTime=Balance.CAMP_WAKE_TIME;w.px=xs[dir];w.py=ys[dir];w.update(1,0,0,true,false,false);
            for(int pose=0;pose<6;pose++){w.restTime=pose*120;w.clock=pose*120;RenderTest.save(art,w,"camp-rest-"+dir+"-"+pose);}
        }
        w.enterCamp(false);w.campTime=Balance.CAMP_WAKE_TIME;w.px=148;w.py=151;w.update(1,0,0,true,false,false);w.restState=World.REST_IDLE;RenderTest.save(art,w,"camp-rest-from-fire-center");
        System.out.println("PASS: All 24 rest poses use binary alpha, exact idle transitions, stable soles and transparent contrasting backgrounds.");
        System.out.println("PASS: Sitting/standing retain the same pose when cancelled, breathing loops repeat, and rest stays isolated from combat.");
        System.out.println("PASS: Exported resting beside the bonfire from all four directions and safely outside its center.");System.exit(0);
    }
}
