import javax.microedition.lcdui.Graphics;

/** Small bitmap accents keep the entrance quotation readable without firmware fonts. */
public final class VietnameseText {
    private static final String LOWER="àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ";
    private static final String UPPER="ÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ";
    private static void dot(Graphics g,int x,int y,int scale){g.fillRect(x,y,scale,scale);}
    public static void center(Graphics g,String s,int y,int color,int scale){draw(g,s,(320-(s.length()*4-1)*scale)/2,y,color,scale);}
    public static void draw(Graphics g,String s,int x,int y,int color,int scale){
        for(int i=0;i<s.length();i++){
            char ch=s.charAt(i);int p=UPPER.indexOf(ch);if(p<0)p=LOWER.indexOf(ch);
            int mark=0,tone=0;char base=ch;
            if(p>=0){
                if(p<17){base='A';if(p<5)tone=p+1;else if(p<11){mark=1;tone=p-5;}else{mark=2;tone=p-11;}}
                else if(p<28){base='E';if(p<22)tone=p-16;else{mark=2;tone=p-22;}}
                else if(p<33){base='I';tone=p-27;}
                else if(p<50){base='O';if(p<38)tone=p-32;else if(p<44){mark=2;tone=p-38;}else{mark=3;tone=p-44;}}
                else if(p<61){base='U';if(p<55)tone=p-49;else{mark=3;tone=p-55;}}
                else if(p<66){base='Y';tone=p-60;}
                else base='D';
            }else if(base>='a'&&base<='z')base=(char)(base-32);
            int xx=x+i*4*scale;Art.text(g,String.valueOf(base),xx,y,color,scale);g.setColor(color);
            if(p==66)g.fillRect(xx-scale,y+2*scale,3*scale,scale);
            if(mark==1){dot(g,xx,y-2*scale,scale);dot(g,xx+scale,y-scale,scale);dot(g,xx+2*scale,y-2*scale,scale);}
            if(mark==2){dot(g,xx,y-2*scale,scale);dot(g,xx+scale,y-3*scale,scale);dot(g,xx+2*scale,y-2*scale,scale);}
            if(mark==3){dot(g,xx+2*scale,y-scale,scale);dot(g,xx+3*scale,y-2*scale,scale);}
            int ay=y-(mark==1||mark==2?5:2)*scale;
            if(tone==1){dot(g,xx,ay-scale,scale);dot(g,xx+scale,ay,scale);}
            if(tone==2){dot(g,xx+scale,ay,scale);dot(g,xx+2*scale,ay-scale,scale);}
            if(tone==3){dot(g,xx+scale,ay-2*scale,scale);dot(g,xx+2*scale,ay-scale,scale);dot(g,xx+scale,ay,scale);}
            if(tone==4){dot(g,xx,ay,scale);dot(g,xx+scale,ay-scale,scale);dot(g,xx+2*scale,ay,scale);}
            if(tone==5)dot(g,xx+scale,y+6*scale,scale);
        }
    }
    private VietnameseText(){}
}
