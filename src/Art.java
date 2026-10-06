import javax.microedition.lcdui.*;
/** Cached pixel scenery and supplied sprite atlas, using only MIDP 2.0 drawing. */
public final class Art {
    private Image floor, warned;
    private final HeroSprites heroSprites=new HeroSprites();
    private final BossSprites bossSprites=new BossSprites();
    private int cached=-1;
    private static final String LETTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-:/!.+? ";
    private static final String[] GLYPHS={"010101111101101","110101110101110","011100100100011","110101101101110","111100110100111","111100110100100","011100101101011","101101111101101","111010010010111","001001001101010","101101110101101","100100100100111","101111111101101","101111111111101","010101101101010","110101110100100","010101101111011","110101110101101","011100010001110","111010010010010","101101101101111","101101101101010","101101111111101","101101010101101","101101010010010","111001010100111","111101101101111","010110010010111","110001010100111","110001010001110","101101111001001","111100110001110","011100111101111","111001010010010","111101111101111","111101111001110","000000111000000","000010000010000","001001010100100","010010010000010","000000000000010","000010111010000","110001010000010","000000000000000"};
    public static void text(Graphics g,String s,int x,int y,int color,int scale){
        g.setColor(color);for(int i=0;i<s.length();i++){int c=LETTERS.indexOf(s.charAt(i));if(c<0)c=LETTERS.length()-1;String p=GLYPHS[c];for(int k=0;k<15;k++)if(p.charAt(k)=='1')g.fillRect(x+i*4*scale+(k%3)*scale,y+(k/3)*scale,scale,scale);}
    }
    private void center(Graphics g,String s,int y,int c,int scale){text(g,s,(320-(s.length()*4-1)*scale)/2,y,c,scale);}
    private void rect(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    public Art(){
        floor=Image.createImage(320,240);Graphics g=floor.getGraphics();
        rect(g,0x11131c,0,0,320,240);
        // Build the weathered arena once; fine stone texture costs no work per frame.
        for(int y=47;y<222;y+=14)for(int x=-14;x<320;x+=28){
            int xx=x+((y/14)%2)*14,k=((x+400)*17+y*13)%5;
            int light=Math.max(0,9-Math.abs(xx-151)/19-Math.abs(y-130)/24);
            rect(g,0x23232c+(k+light)*0x010101,xx+1,y+1,26,12);
            g.setColor(0x38353f+light*0x010101);g.drawLine(xx+2,y+1,xx+25,y+1);
            g.setColor(0x151822);g.drawLine(xx+2,y+12,xx+25,y+12);g.drawLine(xx+26,y+3,xx+26,y+11);
            if(k<2){g.setColor(0x191c26);g.drawLine(xx+7,y+4,xx+12,y+7);g.drawLine(xx+12,y+7,xx+10,y+11);}
            rect(g,0x41404a,xx+5+k*3,y+4+k,1,1);
        }
        // An extinguished sun seal in the floor; subdued beneath attack telegraphs.
        g.setColor(0x544b49);g.drawArc(78,72,164,126,0,360);
        g.setColor(0x3e383d);g.drawArc(81,75,158,120,0,360);g.drawArc(85,78,150,114,0,360);
        g.setColor(0x49444b);g.drawArc(123,104,74,61,0,360);
        for(int i=0;i<12;i++){
            double a=i*Math.PI/6;int x=160+(int)(75*Math.cos(a)),y=135+(int)(57*Math.sin(a));
            g.setColor(0x63544a);g.drawLine(x,y,x+(int)(5*Math.cos(a)),y+(int)(4*Math.sin(a)));
            rect(g,0x82715b,x-1,y-1,2,2);
        }
        g.setColor(0x11151e);g.drawLine(95,157,122,150);g.drawLine(122,150,140,153);g.drawLine(140,153,157,140);
        g.drawLine(220,84,202,101);g.drawLine(202,101,207,118);g.drawLine(207,118,198,126);
        // The broken rear wall has recessed windows, eroded bevels and a torn standard.
        for(int x=0;x<320;x+=32){
            rect(g,0x3c3742,x,27,31,18);rect(g,0x625761,x+1,28,29,2);
            rect(g,0x24232f,x+2,42,28,5);rect(g,0x161a24,x+5,46,23,5);
            rect(g,0x4b434c,x+27,31,2,9);
        }
        for(int x=61;x<285;x+=88){
            rect(g,0x181b28,x,29,22,13);rect(g,0x756471,x-2,28,2,14);rect(g,0x45404b,x+22,28,2,14);
            rect(g,0x34303c,x+7,30,2,11);rect(g,0x34303c,x+14,30,2,11);
        }
        rect(g,0x65504e,152,29,19,2);rect(g,0x492e3d,155,31,13,24);rect(g,0x6b4050,156,32,2,16);
        g.setColor(0x171b25);g.fillTriangle(159,50,161,56,163,49);g.fillTriangle(166,45,169,54,169,44);
        rect(g,0x957559,160,37,3,5);rect(g,0x957559,158,39,7,1);
        for(int y=45;y<223;y+=27){pillar(g,1,y);pillar(g,305,y);}
        // Ash and masonry stay close to the edges, leaving the fight readable.
        for(int i=0;i<42;i++){
            int x=i%2==0?17+(i*19)%21:280+(i*13)%22,y=53+(i*47)%160;
            rect(g,0x141822,x-2,y+2,7,3);rect(g,0x4b4148,x,y,4,3);rect(g,0x74616a,x,y,3,1);
        }
        for(int i=0;i<24;i++){int x=35+(i*83)%246,y=58+(i*47)%145;rect(g,0x151923,x,y,3,1);rect(g,0x504751,x+2,y-1,1,1);}
        rect(g,0x10141e,0,222,320,18);rect(g,0x574750,15,219,290,3);rect(g,0x77635b,17,219,286,1);
        // Foreground doorway, approached from below at the start.
        rect(g,0x090c14,141,207,38,33);rect(g,0x514750,134,202,7,38);rect(g,0x867067,135,203,2,37);
        rect(g,0x514750,179,202,7,38);rect(g,0x867067,180,203,2,37);rect(g,0x49404e,137,199,46,6);
        rect(g,0x8b7064,138,199,43,1);
        for(int y=208;y<240;y+=9){rect(g,0x262632,134,y,7,1);rect(g,0x262632,179,y,7,1);}
        for(int x=35;x<300;x+=245){rect(g,0x453a3d,x-6,43,13,7);rect(g,0x191b25,x-5,50,11,5);}
    }
    void pillar(Graphics g,int x,int y){rect(g,0x151821,x,y,14,26);rect(g,0x4a4850,x+2,y,9,19);rect(g,0x636068,x+3,y+1,2,16);rect(g,0x303039,x+8,y+2,3,19);rect(g,0x69616a,x,y+19,14,3);}
    void flame(Graphics g,int x,int y,int tick){int f=(tick/110)%3;rect(g,0x462928,x-5,y-6,11,12);rect(g,0x7b3d24,x-3,y-7-f,7,12);rect(g,0xd77833,x-2,y-5-f,5,9);rect(g,0xf9c467,x-1,y-3,3,6);rect(g,0x2c2730,x-4,y+5,9,3);}
    void telegraph(World w){
        if(cached==w.hazardVersion)return;cached=w.hazardVersion;if(warned==null)warned=Image.createImage(320,240);Graphics g=warned.getGraphics();g.drawImage(floor,0,0,Graphics.TOP|Graphics.LEFT);
        for(int y=49;y<217;y++)for(int x=16;x<304;x++)if(w.inHazard(x,y,false)){
            boolean edge=!w.inHazard(x-1,y,false)||!w.inHazard(x+1,y,false)||!w.inHazard(x,y-1,false)||!w.inHazard(x,y+1,false);
            if(edge||(x+y)%4==0){g.setColor(edge?0xffb560:0x963e3b);g.fillRect(x,y,1,1);}
        }
    }
    public void draw(Graphics g,World w,boolean paused){
        if(w.mode==World.TITLE){mainMenu(g,w);return;}
        if(w.mode==World.STORY){story(g,w);if(paused){panel(g,42,88,236,67);center(g,"PAUSED",99,0xe5c992,2);center(g,"P - CONTINUE",128,0xd2c4b0,1);}return;}
        boolean hazard=w.mode==World.FIGHT&&(w.bstate==World.WARNING||w.bstate==World.ACTIVE);
        if(hazard){telegraph(w);g.drawImage(warned,0,0,Graphics.TOP|Graphics.LEFT);}else g.drawImage(floor,0,0,Graphics.TOP|Graphics.LEFT);
        flame(g,41,42,w.clock);flame(g,286,42,w.clock);
        if(w.mode==World.INTRO){rect(g,0xd8b881,145,215,30,25);center(g,"THE GATE CLOSES BEHIND YOU",28,0xccb892,1);}
        else{for(int i=0;i<6;i++)rect(g,0x62616a,143+i*6,207,2,33);}
        if(hazard){
            if(w.bstate==World.ACTIVE){g.setColor(0xffe3a1);if(w.attack==3){int r=Balance.QUAKE_RADIUS*w.bt/Balance.ACTIVE[3];g.drawArc((int)w.ox-r,(int)w.oy-r,r*2,r*2,0,360);}else if(w.attack==1){for(int i=-2;i<=2;i++)g.drawLine((int)w.ox+i,(int)w.oy,(int)(w.ox+w.bdx*Balance.SLAM_LENGTH)+i,(int)(w.oy+w.bdy*Balance.SLAM_LENGTH));}}
        }
        if(w.py<w.by){hero(g,w);boss(g,w);}else{boss(g,w);hero(g,w);}
        // Sparse drifting floor mist, kept away from warning interiors.
        for(int i=0;i<4;i++){int x=(i*91+w.clock/95)%350-30,y=72+i*34;g.setColor(0x4a4850);g.drawLine(x,y,x+12,y);g.setColor(0x393b42);g.drawLine(x+8,y+2,x+28,y+2);}
        if(w.mode!=World.TITLE)hud(g,w);
        if(hazard){
            String[] names={"CRESCENT CLEAVE","EXECUTION","IRON CHARGE","ASHEN QUAKE"};
            center(g,names[w.attack],28,0xffcc80,1);
            if(w.bstate==World.WARNING){int ww=w.bt*74/w.warningTime();rect(g,0x4f3437,123,35,74,2);rect(g,0xfac87b,123,35,ww,2);}
        }
        if(w.phaseBanner>0&&w.mode==World.FIGHT&&!hazard)center(g,"II - THE OATH IS BROKEN",28,0xf4bc79,1);
        if(w.mode==World.WIN||w.mode==World.LOSE){
            if(w.deathTime>Balance.DEATH_TIME){panel(g,42,75,236,109);center(g,w.mode==World.WIN?"OATH BROKEN":"YOU FELL",87,w.mode==World.WIN?0xe5c992:0xd88683,2);
            center(g,w.mode==World.WIN?"THE PATH TO THE HEART OF DAWN IS OPEN.":"WATCH. WAIT. TRY AGAIN.",110,0xb4a7a5,1);
            center(g,"HITS LANDED: "+w.hitsLanded,130,0xc7bca3,1);center(g,"CENTER / J - PLAY AGAIN",151,0xf5ca86,1);center(g,"Q - TITLE",169,0xa39ba5,1);}
        }
        if(paused){panel(g,42,88,236,67);center(g,"PAUSED",99,0xe5c992,2);center(g,"P / SOFTKEY - CONTINUE",128,0xd2c4b0,1);center(g,"Q - TITLE",142,0xa39ba5,1);}
    }
    private final World actors=new World();
    private void landscape(Graphics g,int tick){
        rect(g,0x101521,0,0,320,240);
        g.setColor(0x756e77);g.fillArc(240,24,34,34,0,360);g.setColor(0x242333);g.fillArc(251,19,32,35,0,360);
        for(int i=0;i<17;i++)rect(g,0x4e4c5a,13+(i*47)%299,20+(i*19)%69,1,1);
        g.setColor(0x242535);g.fillTriangle(0,115,68,47,124,115);g.fillTriangle(75,120,168,51,248,120);g.fillTriangle(205,117,274,68,320,117);
        for(int i=0;i<9;i++){
            int x=i*39-12,h=30+(i*23)%55;
            rect(g,0x161a27,x,130-h,27,h);rect(g,0x2e303d,x+2,131-h,3,h-3);
            rect(g,0x171c28,x-3,125-h,8,8);rect(g,0x171c28,x+12,125-h,9,8);
            rect(g,0x47404a,x+3,126-h,2,5);rect(g,0x171c28,x+9,123-h,3,9);
            for(int j=0;j<3;j++){
                int wy=139-h+j*18;if(wy<123){rect(g,0x090f1c,x+9,wy,5,9);rect(g,0x38313b,x+8,wy-1,7,1);}
            }
            if(i%3==0){rect(g,0x73503e,x+12,119-h,3,5);rect(g,0xa87243,x+12,120-h,1,3);}
            g.setColor(0x111624);g.drawLine(x+17,135-h,x+13,148-h);g.drawLine(x+13,148-h,x+18,159-h);
        }
        // Ruptured spires and extinguished chimneys on the horizon.
        g.setColor(0x1a1c2b);g.fillTriangle(37,88,44,38,53,88);g.fillTriangle(275,105,281,59,288,105);
        rect(g,0x37313f,43,47,2,35);rect(g,0x101522,44,61,7,4);rect(g,0x34303e,279,71,2,23);
        rect(g,0x111722,0,129,320,111);
        for(int i=0;i<5;i++){int x=(i*87+tick/95)%390-50;g.setColor(0x343442);g.drawLine(x,118+i*7,x+32,118+i*7);g.setColor(0x252b38);g.drawLine(x+18,121+i*7,x+57,121+i*7);}
        for(int i=0;i<11;i++){
            int x=(i*67)%320,y=145+(i*29)%73;
            rect(g,0x1e2430,x,y,21,2);rect(g,0x36313b,x+5,y-3,6,3);rect(g,0x4a3c41,x+5,y-3,4,1);
        }
        for(int i=0;i<14;i++){
            int x=(i*53+tick/190)%320,y=134-(tick/210+i*13)%94;
            rect(g,i%4==0?0x785344:0x49404d,x,y,1,1);
        }
    }
    public void mainMenu(Graphics g,World w){
        landscape(g,w.clock);
        rect(g,0x3a3038,22,103,17,115);rect(g,0x655251,25,104,3,114);rect(g,0x373039,282,103,17,115);rect(g,0x655251,285,104,3,114);
        flame(g,31,108,w.clock);flame(g,291,108,w.clock);
        center(g,"ASHGATE",31,0xebcf9e,4);center(g,"THE BLACK OATH",62,0xb8a9a9,1);
        center(g,"CHAPTER I - THE LAST EMBER",80,0x8d8597,1);
        panel(g,66,100,188,101);
        String[] items={"BEGIN JOURNEY","CONTROLS",w.soundOn?"SOUND: ON":"SOUND: OFF","EXIT"};
        for(int i=0;i<4;i++){int y=114+i*22;if(w.menuIndex==i){rect(g,0x382d34,76,y-5,168,17);g.setColor(0xeac888);g.fillTriangle(82,y-1,86,y+2,82,y+5);g.drawLine(91,y+9,232,y+9);}center(g,items[i],y,w.menuIndex==i?0xffdba0:0x9d95a6,1);}
        center(g,"UP / DOWN - SELECT   J - CONFIRM",216,0xb9ad9b,1);
        center(g,"H - HELP   Q - EXIT",229,0x797383,1);
    }
    public void story(Graphics g,World w){
        int scene=Math.min(Balance.STORY_SCENES-1,w.storyTime/Balance.STORY_SCENE_TIME),t=w.storyTime%Balance.STORY_SCENE_TIME;
        landscape(g,w.clock);
        if(scene==0){
            rect(g,0x38303b,118,83,84,55);rect(g,0x56505b,118,83,4,53);rect(g,0x191d2b,139,92,13,34);rect(g,0x191d2b,170,92,13,34);
            g.setColor(0x51454c);g.fillTriangle(109,84,143,61,158,84);g.fillTriangle(157,84,177,55,210,84);
            rect(g,0x201d29,151,58,12,79);rect(g,0x897764,155,63,2,25);rect(g,0x762f39,157,66,22,14);rect(g,0x111521,171,74,8,6);
            for(int i=0;i<5;i++)flame(g,86+i*38,140+(i%2)*9,w.clock+i*73);
            for(int i=0;i<13;i++)rect(g,0xae6c48,(i*53+w.clock/100)%320,147-(w.clock/65+i*17)%100,1,2);
        }else if(scene==1){
            rect(g,0x1b1c28,76,40,168,119);rect(g,0x4b414b,91,48,10,103);rect(g,0x4b414b,219,48,10,103);rect(g,0x756068,93,49,2,101);rect(g,0x756068,221,49,2,101);
            rect(g,0x392936,134,62,52,67);rect(g,0x69525a,134,62,3,67);rect(g,0x69525a,183,62,3,67);rect(g,0x352937,124,116,72,19);rect(g,0x57505a,100,141,120,5);rect(g,0x35313e,92,147,136,6);
            flame(g,105,82,w.clock);flame(g,216,82,w.clock);
            actors.mode=World.FIGHT;actors.clock=w.clock;actors.bx=160;actors.by=138;actors.bstate=World.PREPARE;actors.bdx=0;actors.bdy=1;actors.phase=1;boss(g,actors);
        }else{
            rect(g,0x292a35,106,38,108,124);rect(g,0x080c14,134,55,52,106);
            rect(g,0x57505a,124,50,10,112);rect(g,0x777071,125,51,2,110);rect(g,0x57505a,186,50,10,112);rect(g,0x777071,187,51,2,110);
            rect(g,0x5f555c,125,45,70,11);rect(g,0x8d7a6b,130,46,61,2);
            int door=Math.min(24,t/100);rect(g,0x382f38,135,58,24-door,101);rect(g,0x382f38,161+door,58,24-door,101);
            flame(g,117,108,w.clock);flame(g,205,108,w.clock);
            actors.mode=World.FIGHT;actors.clock=w.clock;actors.px=160;actors.py=174-Math.min(24,t/120);actors.fy=-1;actors.fx=0;actors.pstate=World.IDLE;actors.inv=0;actors.moving=t<2800;hero(g,actors);
            g.setColor(0x4f4651);g.drawLine(143,164,119,182);g.drawLine(177,164,200,182);
        }
        rect(g,0x080c13,0,0,320,18);rect(g,0x080c13,0,180,320,60);
        String[] headings={"I - THE FALLEN KINGDOM","II - THE BLACK OATH","III - THE LAST KNIGHT"};
        String[] lines1={"THE SUN DIED. THE KINGDOM TURNED TO ASH.","THE OATHKEEPER GUARDS THE LAST EMBER.","YOU ARE THE LAST KNIGHT OF THE FALLEN."};
        String[] lines2={"A BROKEN OATH BOUND THE WORLD TO DEATH.","BEYOND HIS GATE LIES THE HEART OF DAWN.","TAKE BACK THE DAWN. RESTORE THE WORLD."};
        center(g,headings[scene],6,0xddbd8e,1);
        center(g,lines1[scene],188,0xd2c4b5,1);
        if(t>=900)center(g,lines2[scene],202,0xb4a6a8,1);
        text(g,"J / CENTER - SKIP",12,228,0x8b8594,1);text(g,(scene+1)+" / 3",277,228,0x8b8594,1);
        rect(g,0x39303b,12,218,296,2);rect(g,0xb99464,12,218,296*t/Balance.STORY_SCENE_TIME,2);
        // Dithered fade through black uses only opaque pixels, supported by MIDP 2.0.
        int fade=t<450?450-t:t>4050?t-4050:0;
        if(fade>0){int level=fade*16/450;g.setColor(0x080c13);for(int y=0;y<224;y+=4)for(int x=0;x<320;x+=4)if((((x/4)%4)*4+(y/4)%4)<level)g.fillRect(x,y,4,4);}
    }

    public void controls(Graphics g){
        rect(g,0x11151e,0,0,320,240);center(g,"HOW TO SURVIVE",17,0xe8cb92,2);
        String[] rows={"MOVE: D-PAD OR W A S D","DIAGONALS: TWO KEYS / 1 3 7 9","SLASH: CENTER / J / 5","ROLL: DOUBLE-TAP A MOVE KEY","HEAL: L / 2 - 3 FLASKS","PAUSE: P / LEFT SOFTKEY","SOUND: M    EXIT: Q ON TITLE","","AMBER MARKS THE WHOLE DANGER ZONE.","TAP TWICE FAST. HOLD ONLY MOVES.","HEAL TAKES 1 SECOND. HITS CANCEL.","CYAN ROLL GLOW MEANS INVINCIBLE.","STRIKE AFTER THE BOSS COMMITS.","PHASE II STARTS BELOW HALF HEALTH."};
        for(int i=0;i<rows.length;i++)text(g,rows[i],19,46+i*11, i<7?0xc9c9cc:0xb5a68d,1);
        center(g,"CENTER / J - BACK",218,0xffd790,1);
    }
    void panel(Graphics g,int x,int y,int w,int h){rect(g,0x0b0e16,x,y,w,h);g.setColor(0x76604c);g.drawRect(x,y,w,h);g.setColor(0x35303a);g.drawRect(x+3,y+3,w-6,h-6);}
    void hud(Graphics g,World w){
        rect(g,0x10131b,0,0,320,26);text(g,"KNIGHT",7,4,0xd8d8d2,1);rect(g,0x4e2d36,7,14,100,6);rect(g,0xc95f60,7,14,w.hp*100/Balance.PLAYER_HP,6);rect(g,0xed9390,7,14,w.hp*100/Balance.PLAYER_HP,1);
        text(g,"FLASK "+w.potions,122,4,0xddba73,1);for(int i=0;i<3;i++){rect(g,i<w.potions?0xdcac56:0x46424a,124+i*10,14,5,6);rect(g,0xa18b61,125+i*10,12,3,2);}
        text(g,"ROLL",178,4,0x9ac9c9,1);rect(g,0x293b45,178,14,46,5);rect(g,w.dodgeCd==0?0x7fdad0:0x4b7b83,178,14,46*(Balance.ROLL_COOLDOWN-w.dodgeCd)/Balance.ROLL_COOLDOWN,5);
        text(g,"P:PAUSE",247,4,0x938e9b,1);text(g,w.pstate==World.HEAL?"DRINKING":w.rollingSafe()?"IMMUNE":w.pstate==World.ATTACK?"SLASH":"",247,15,0xf5ca86,1);
        if(w.pstate==World.HEAL){rect(g,0x433a31,(int)w.px-13,(int)w.py-34,26,3);rect(g,0xf0c068,(int)w.px-13,(int)w.py-34,26*w.pt/Balance.HEAL_TIME,3);}
        rect(g,0x10131b,25,224,270,16);text(g,w.phase==2?"II BLACK OATHKEEPER":"BLACK OATHKEEPER",31,225,0xc7b6a3,1);rect(g,0x493039,31,233,258,4);rect(g,w.phase==2?0xd47857:0xb34b5a,31,233,258*w.bossHp/Balance.BOSS_HP,4);
        if(w.bstate==World.RECOVER&&w.mode==World.FIGHT)text(g,"OPEN",(int)w.bx-7,Math.max(27,(int)w.by-KnightArt.height(true)-7),0xcfe5b4,1);
    }
    /** Dominant movement axis maps eight-way movement to four drawn facings. */
    static int facing(float dx,float dy){return Math.abs(dx)>=Math.abs(dy)?(dx<0?2:3):(dy<0?0:1);}
    void hero(Graphics g,World w){
        int x=(int)w.px,y=(int)w.py;g.setColor(0x13161c);g.fillArc(x-10,y-3,20,7,0,360);
        if(w.mode==World.LOSE){heroSprites.draw(g,w,facing(w.fx,w.fy));return;}
        if(w.inv>0&&(w.clock/65)%2==0)return;
        int spriteDir=w.pstate==World.ROLL?facing(w.rollX,w.rollY):facing(w.fx,w.fy);
        heroSprites.draw(g,w,spriteDir);
        if(w.pstate==World.ROLL){g.setColor(0x76d7d1);g.drawArc(x-10,y-16,20,18,0,360);}
    }
    void blade(Graphics g,int x,int y,int ex,int ey,boolean large){
        boolean horizontal=Math.abs(ex-x)>=Math.abs(ey-y);int nx=horizontal?0:1,ny=horizontal?1:0,width=large?3:1;
        g.setColor(0x10151f);g.drawLine(x-nx*(width+1),y-ny*(width+1),ex-nx*(width+1),ey-ny*(width+1));g.drawLine(x+nx*(width+1),y+ny*(width+1),ex+nx*(width+1),ey+ny*(width+1));
        for(int i=-width;i<=width;i++){g.setColor(i<0?0x637c89:i==0?0xd2ded2:0x92a8ae);g.drawLine(x+nx*i,y+ny*i,ex+nx*i,ey+ny*i);}
        int guard=large?7:4,grip=large?5:3;
        g.setColor(0x8c602c);g.drawLine(x-nx*guard,y-ny*guard,x+nx*guard,y+ny*guard);g.setColor(0xe2b75c);g.drawLine(x-nx*(guard-1),y-ny*(guard-1),x+nx*(guard-1),y+ny*(guard-1));
        int hx=ex>x?-grip:ex<x?grip:0,hy=ey>y?-grip:ey<y?grip:0;g.setColor(0x684f33);g.drawLine(x,y,x+hx,y+hy);rect(g,0xe2b75c,x+hx-1,y+hy-1,large?3:2,large?3:2);
    }
    void boss(Graphics g,World w){
        int x=(int)w.bx,y=(int)w.by;g.setColor(0x11131a);g.fillArc(x-27,y-5,54,12,0,360);
        int dir=facing(w.bdx,w.bdy),sign=dir==2?-1:1;boolean back=dir==0,side=dir>=2;
        boolean walking=w.bstate==World.SEEK&&World.length(w.px-w.bx,w.py-w.by)>48;
        int step=walking?(w.clock/110)%8:0;
        if(bossSprites.draw(g,w,dir,step,walking)){
            if(w.bstate==World.ACTIVE&&w.attack==0&&w.mode!=World.WIN){g.setColor(0xd6cbb7);g.drawArc(x-56,y-56,112,86,w.bdy>0?190:10,160);}
            bossHit(g,w,x,y);return;
        }
        if(w.mode==World.WIN){KnightArt.fallen(g,x,y,true);blade(g,x-12,y+3,x-58,y+6,true);return;}
        int pose=w.bstate==World.PREPARE||w.bstate==World.WARNING||w.bstate==World.ACTIVE?World.ATTACK:World.IDLE;
        if((w.events&World.BOSS_HIT)!=0&&pose==World.IDLE)pose=World.HURT;
        int poseTime=w.bstate==World.ACTIVE?200:0;
        KnightArt.bossBody(g,x,y,dir,step,walking,w.clock,pose,poseTime,w.phase);
        float dx=w.bdx,dy=w.bdy;int hx=SkeletonBossArt.handX(x,dir,pose,poseTime,step,walking),hy=SkeletonBossArt.handY(y,dir,pose,poseTime,step,walking),ex=hx+(side?sign*34:back?22:-22),ey=hy+36;
        // Raised sword stays alongside the open helmet, inside its 88-pixel height.
        if(w.bstate==World.PREPARE||w.bstate==World.WARNING){ex=hx+(side?sign*38:back?32:-32);ey=hy-20;}
        if(w.bstate==World.ACTIVE){ex=hx+(int)(dx*48);ey=hy+(int)(dy*38);if(w.attack==0){g.setColor(0xd6cbb7);g.drawArc(x-56,y-56,112,86,dy>0?190:10,160);}}
        blade(g,hx,hy,ex,ey,true);
        bossHit(g,w,x,y);
    }
    private void bossHit(Graphics g,World w,int x,int y){if((w.events&World.BOSS_HIT)!=0&&w.mode!=World.WIN){g.setColor(0xffeac0);for(int i=0;i<4;i++)g.drawLine(x-20+i*13,y-38,x-24+i*15,y-46);}}
}
