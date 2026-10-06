import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
public class AtlasTest {
    static void checkAnimationAlpha(Image atlas,int action,int dir,int frame){
        int width=atlas.getWidth()/6,height=atlas.getHeight()/24;
        int[] pixels=new int[width*height];atlas.getRGB(pixels,0,width,frame*width,(action*4+dir)*height,width,height);
        int opaque=0,top=height,bottom=-1;
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            int alpha=pixels[y*width+x]>>>24;
            if(alpha!=0&&alpha!=255)throw new RuntimeException("Non-binary animation alpha: "+action+"/"+dir+"/"+frame);
            if(alpha==255){
                opaque++;top=Math.min(top,y);bottom=Math.max(bottom,y);
                if(x==0||x==width-1||y==0||y==height-1)throw new RuntimeException("Animation clipped at cell edge: "+action+"/"+dir+"/"+frame);
            }
        }
        if(opaque<300||opaque>1100||bottom-top+1<38||bottom-top+1>(action==3?56:48))
            throw new RuntimeException("Animation matte or scale invalid: "+action+"/"+dir+"/"+frame+" area="+opaque+" height="+(bottom-top+1));
        if(bottom!=52)throw new RuntimeException("Ground anchor moved: "+action+"/"+dir+"/"+frame+" y="+bottom);
    }
    static int[] healingSoles(Image atlas,int dir,int frame){
        int width=atlas.getWidth()/6;int[] pixels=new int[width*64];atlas.getRGB(pixels,0,width,frame*width,(8+dir)*64,width,64);
        int left=width,right=-1;
        for(int y=51;y<=52;y++)for(int x=width/2-15;x<width/2+15;x++)if((pixels[y*width+x]>>>24)==255){left=Math.min(left,x);right=Math.max(right,x);}
        if(right<left)throw new RuntimeException("Missing planted healing soles: "+dir+"/"+frame);
        return new int[]{left,right};
    }
    static long pixelHash(int[] pixels){long hash=1;for(int pixel:pixels)hash=31*hash+pixel;return hash;}
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,AtlasTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        HeroSprites sprites=new HeroSprites();World w=new World();w.mode=World.FIGHT;
        Image atlas=Image.createImage(AtlasTest.class.getResourceAsStream("/hero-atlas.png"));
        if(atlas.getWidth()!=480||atlas.getHeight()!=1536)throw new RuntimeException("Unexpected atlas dimensions");
        int cellWidth=atlas.getWidth()/6,cellHeight=atlas.getHeight()/24;
        long[][] walkHashes=new long[4][6];
        int[][] soleLeft=new int[4][6],soleRight=new int[4][6];
        int[] dirs={1,2,3,0},states={World.IDLE,World.ROLL,World.HEAL,World.ATTACK,World.IDLE,World.IDLE},swordTimes={0,75,150,200,250,415};
        String[] labels={"IDLE","ROLL","HEAL","SLASH","WALK","DEATH"},heads={"DOWN","LEFT","RIGHT","UP"};
        for(int frame=0;frame<6;frame++){
            Image im=Image.createImage(320,240);Graphics g=im.getGraphics();g.setColor(0x18212a);g.fillRect(0,0,320,240);
            Image death=Image.createImage(320,240);Graphics dg=death.getGraphics();dg.setColor(0x18212a);dg.fillRect(0,0,320,240);Art.text(dg,"DEATH - PLAYS ONCE",72,45,0xd0b7da,1);
            for(int d=0;d<4;d++)Art.text(g,heads[d],65+d*60,9,0xc3b7dc,1);
            for(int action=0;action<6;action++)for(int d=0;d<4;d++){
                w.mode=action==5?World.LOSE:World.FIGHT;w.deathTime=frame*Balance.DEATH_FRAME_TIME;
                w.px=80+d*60;w.py=action==5?155:53+action*46;w.pstate=states[action];w.moving=action==4;w.clock=frame*(action==4?95:170);
                w.pt=action==1?(Balance.ROLL_TIME*frame+5)/6:action==2?(Balance.HEAL_TIME*frame+5)/6:action==3?swordTimes[frame]:0;
                sprites.draw(action==5?dg:g,w,dirs[d]);if(action<5)Art.text(g,labels[action],5,29+action*46,0xa99ab9,1);else Art.text(dg,heads[d],65+d*60,170,0xc3b7dc,1);
                Image cell=Image.createImage(cellWidth,cellHeight);Graphics cg=cell.getGraphics();cg.setColor(0x18212a);cg.fillRect(0,0,cellWidth,cellHeight);float px=w.px,py=w.py;w.px=cellWidth/2;w.py=52;sprites.draw(cg,w,dirs[d]);w.px=px;w.py=py;
                int[] rgb=new int[cellWidth*cellHeight];cell.getRGB(rgb,0,cellWidth,0,0,cellWidth,cellHeight);int visible=0,background=0;for(int c:rgb){if((c&0xffffff)==0x18212a)background++;else visible++;}
                if(visible<40||background<200)throw new RuntimeException("Empty or opaque atlas frame: "+action+"/"+d+"/"+frame);
                if(action==0||action==2||action==3||action==4){
                    checkAnimationAlpha(atlas,action,d,frame);if(action==4)walkHashes[d][frame]=pixelHash(rgb);
                    if(action==2){int[] soles=healingSoles(atlas,d,frame);soleLeft[d][frame]=soles[0];soleRight[d][frame]=soles[1];}
                    Image contrast=Image.createImage(cellWidth,cellHeight);Graphics contrastGraphics=contrast.getGraphics();contrastGraphics.setColor(0xe0c090);contrastGraphics.fillRect(0,0,cellWidth,cellHeight);
                    w.px=cellWidth/2;w.py=52;sprites.draw(contrastGraphics,w,dirs[d]);w.px=px;w.py=py;
                    int[] other=new int[cellWidth*cellHeight];contrast.getRGB(other,0,cellWidth,0,0,cellWidth,cellHeight);
                    for(int pixel=0;pixel<cellWidth*cellHeight;pixel++)if((rgb[pixel]&0xffffff)==0x18212a&&(other[pixel]&0xffffff)!=0xe0c090)
                        throw new RuntimeException("Animation backdrop not transparent on contrasting color: "+action+"/"+d+"/"+frame);
                }
            }
            int[] rgb=new int[76800];im.getRGB(rgb,0,320,0,0,320,240);BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/sprite-actions-"+frame+".png"));
            death.getRGB(rgb,0,320,0,0,320,240);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/death-animation-"+frame+".png"));
        }

        for(int d=0;d<4;d++)for(int a=0;a<6;a++)for(int b=a+1;b<6;b++)if(walkHashes[d][a]==walkHashes[d][b])
            throw new RuntimeException("Duplicate walk frame: "+d+"/"+a+"/"+b);
        for(int d=0;d<4;d++)for(int frame=1;frame<6;frame++)if(Math.abs(soleLeft[d][frame]-soleLeft[d][0])>1||Math.abs(soleRight[d][frame]-soleRight[d][0])>1)
            throw new RuntimeException("Healing feet moved sideways: "+d+"/"+frame);
        System.out.println("PASS: All 144 sprite frames load, render and preserve transparency.");
        System.out.println("PASS: All 96 matched idle/heal/attack/walk frames render with binary alpha, stable ground anchors, clear sword tips and contrasting backgrounds.");System.exit(0);
    }
}
