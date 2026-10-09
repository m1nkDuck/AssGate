import javax.microedition.lcdui.*;
/** Cached pixel scenery and supplied sprite atlas, using only MIDP 2.0 drawing. */
public final class Art {
    private Image floor, warned, campFloor;
    private final HeroSprites heroSprites=new HeroSprites();
    private final BossSprites bossSprites=new BossSprites();
    private int cached=-1;
    private Image belfryFloor,belfryWarned;
    private Belfry belfryHazardOwner;
    private int belfryFloorKey=-1,belfryHazardKey=-1,bellHazardKey=-1,belfryHazardRoom=-1;
    private final int[] chapterActors=new int[5];
    private final float[] chapterDepth=new float[5];
    private static final String LETTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-:/!.+? ";
    private static final String[] GLYPHS={"010101111101101","110101110101110","011100100100011","110101101101110","111100110100111","111100110100100","011100101101011","101101111101101","111010010010111","001001001101010","101101110101101","100100100100111","101111111101101","101111111111101","010101101101010","110101110100100","010101101111011","110101110101101","011100010001110","111010010010010","101101101101111","101101101101010","101101111111101","101101010101101","101101010010010","111001010100111","111101101101111","010110010010111","110001010100111","110001010001110","101101111001001","111100110001110","011100111101111","111001010010010","111101111101111","111101111001110","000000111000000","000010000010000","001001010100100","010010010000010","000000000000010","000010111010000","110001010000010","000000000000000"};
    public static void text(Graphics g,String s,int x,int y,int color,int scale){
        g.setColor(color);for(int i=0;i<s.length();i++){int c=LETTERS.indexOf(s.charAt(i));if(c<0)c=LETTERS.length()-1;String p=GLYPHS[c];for(int k=0;k<15;k++)if(p.charAt(k)=='1')g.fillRect(x+i*4*scale+(k%3)*scale,y+(k/3)*scale,scale,scale);}
    }
    private void center(Graphics g,String s,int y,int c,int scale){text(g,s,(320-(s.length()*4-1)*scale)/2,y,c,scale);}
    private void rect(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    public Art(){floor=ArenaArt.create();}
    void flame(Graphics g,int x,int y,int tick){int f=(tick/110)%3;rect(g,0x462928,x-5,y-6,11,12);rect(g,0x7b3d24,x-3,y-7-f,7,12);rect(g,0xd77833,x-2,y-5-f,5,9);rect(g,0xf9c467,x-1,y-3,3,6);rect(g,0x2c2730,x-4,y+5,9,3);}
    void telegraph(World w){
        if(cached==w.hazardVersion)return;cached=w.hazardVersion;
        int[] pixels=new int[320*240];
        for(int y=49;y<217;y++)for(int x=16;x<304;x++)if(w.inHazard(x,y,false)){
            boolean edge=!w.inHazard(x-1,y,false)||!w.inHazard(x+1,y,false)||!w.inHazard(x,y-1,false)||!w.inHazard(x,y+1,false);
            if(edge||(x+y)%4==0)pixels[y*320+x]=edge?0xffffb560:0xff963e3b;
        }
        warned=Image.createRGBImage(pixels,320,240,true);
    }
    public void draw(Graphics g,World w,boolean paused){
        if(w.mode==World.TITLE){mainMenu(g,w);return;}
        if(w.mode==World.STORY){story(g,w);if(paused){panel(g,42,88,236,67);center(g,"PAUSED",99,0xe5c992,2);center(g,"P - CONTINUE",128,0xd2c4b0,1);}return;}
        if(w.mode==World.CAMP){camp(g,w);if(paused)pausePanel(g);return;}
        if(w.mode==World.BELFRY||w.mode==World.BELFRY_LOSE||w.mode==World.DAWN){belfry(g,w);if(paused)pausePanel(g);return;}
        boolean hazard=w.mode==World.FIGHT&&(w.bstate==World.WARNING||w.bstate==World.ACTIVE);
        g.drawImage(floor,0,0,Graphics.TOP|Graphics.LEFT);ArenaArt.ambient(g,w.clock);
        if(w.mode==World.INTRO){rect(g,0x27322c,145,215,30,25);center(g,"THE GATE CLOSES BEHIND YOU",28,0xccb892,1);}
        else{for(int i=0;i<6;i++){rect(g,0x50433a,143+i*6,207,2,33);rect(g,0x786046,143+i*6,210+i*2,2,2);}}
        if(hazard){telegraph(w);g.drawImage(warned,0,0,Graphics.TOP|Graphics.LEFT);}
        if(hazard){
            if(w.bstate==World.ACTIVE){g.setColor(0xffe3a1);if(w.attack==3){int r=Balance.QUAKE_RADIUS*w.bt/Balance.ACTIVE[3];g.drawArc((int)w.ox-r,(int)w.oy-r,r*2,r*2,0,360);}else if(w.attack==1){for(int i=-2;i<=2;i++)g.drawLine((int)w.ox+i,(int)w.oy,(int)(w.ox+w.bdx*Balance.SLAM_LENGTH)+i,(int)(w.oy+w.bdy*Balance.SLAM_LENGTH));}}
        }
        if(w.py<w.by){hero(g,w);boss(g,w);}else{boss(g,w);hero(g,w);}
        if(w.mode!=World.TITLE)hud(g,w);
        if(hazard){
            String[] names={"CRESCENT CLEAVE","EXECUTION","IRON CHARGE","ASHEN QUAKE"};
            center(g,names[w.attack],28,0xffcc80,1);
            if(w.bstate==World.WARNING){int ww=w.bt*74/w.warningTime();rect(g,0x4f3437,123,35,74,2);rect(g,0xfac87b,123,35,ww,2);}
        }
        if(w.phaseBanner>0&&w.mode==World.FIGHT&&!hazard)center(g,"II - THE OATH IS BROKEN",28,0xf4bc79,1);
        if(w.mode==World.WIN||w.mode==World.LOSE){
            if(w.mode==World.WIN&&w.deathTime<Balance.VICTORY_WAIT)center(g,"THE OATHKEEPER HAS FALLEN",28,0xe5c992,1);
            else if(w.collapseTime()<Balance.DEATH_TIME)center(g,w.mode==World.WIN?"YOUR STRENGTH LEAVES YOU":"YOU FELL",28,0xd8a397,1);
            fade(g,w.fadeOutTime(),Balance.FADE_OUT_TIME);
        }
        if(paused)pausePanel(g);
    }
    private void pausePanel(Graphics g){panel(g,42,88,236,67);center(g,"PAUSED",99,0xe5c992,2);center(g,"P / SOFTKEY - CONTINUE",128,0xd2c4b0,1);center(g,"Q - TITLE",142,0xa39ba5,1);}
    /** Opaque ordered dithering avoids requiring device-specific alpha blending. */
    private void fade(Graphics g,int amount,int duration){
        if(amount<=0)return;
        if(amount>=duration){rect(g,0x000000,0,0,320,240);return;}
        int level=Math.max(1,amount*16/duration);int[] bayer={0,8,2,10,12,4,14,6,3,11,1,9,15,7,13,5};
        g.setColor(0x000000);
        for(int y=0;y<240;y+=4)for(int x=0;x<320;x+=4)if(bayer[(y/4%4)*4+x/4%4]<level)g.fillRect(x,y,4,4);
    }
    public void camp(Graphics g,World w){
        if(campFloor==null)campFloor=BonfireArt.create();
        g.drawImage(campFloor,0,0,Graphics.TOP|Graphics.LEFT);BonfireArt.ambient(g,w.clock);
        BelfryArt.campExit(g,w.oathkeeperComplete);
        if(w.py<151){hero(g,w);BonfireArt.fire(g,w.clock);}else{BonfireArt.fire(g,w.clock);hero(g,w);}
        rect(g,0x0a111c,0,0,320,18);text(g,"EMBER REFUGE",10,6,0xe8c891,1);text(g,"HP "+w.hp+" / FLASK "+w.potions,126,6,0xc7c6bd,1);text(g,"SAFE",222,6,0xa8c5b0,1);text(g,"P:PAUSE",277,6,0x8b939c,1);
        rect(g,0x0a111c,0,212,320,28);
        boolean waking=w.campTime<Balance.CAMP_WAKE_TIME;
        if(waking){center(g,"YOU WAKE BESIDE THE LAST EMBER",220,0xddc4a8,1);}
        else{
            String prompt=w.restState==World.REST_RISE?"STANDING UP":w.restState==World.REST_SIT?"SETTLING BESIDE THE FLAME":w.restState==World.REST_IDLE?"J / CENTER OR MOVE - STAND UP":w.oathkeeperComplete&&World.length(w.px-54,w.py-87)<=28?"J / CENTER - THE ASHEN BELFRY":World.length(w.px-274,w.py-87)<=28?"THE WAY BEHIND YOU IS SEALED":World.length(w.px-148,w.py-151)<=45?"J / CENTER - REST AT THE BONFIRE":"APPROACH THE FIRE OR THE LEFT EXIT";
            center(g,prompt,218,0xddc4a8,1);center(g,w.restState!=World.REST_NONE?"RESTING AT THE BONFIRE   Q - TITLE":"D-PAD / WASD - MOVE   Q - TITLE",230,0x9da9b3,1);
        }
        if(!waking)center(g,w.campNotice>0?"HEALTH AND FLASKS REKINDLED":w.oathkeeperComplete?"THE BELLS CALL. YOUR JOURNEY CONTINUES.":"THE FLAME CALLS YOU BACK",25,w.campNotice>0?0xf4ce8b:0xa3b6bf,1);
        fade(g,Math.max(0,Balance.FADE_IN_TIME-w.campTime),Balance.FADE_IN_TIME);
    }
    private void belfryTelegraph(Belfry b){
        if(belfryWarned!=null&&belfryHazardOwner==b&&belfryHazardRoom==b.room&&belfryHazardKey==b.hazardVersion&&bellHazardKey==b.boss.hazardVersion)return;
        belfryHazardOwner=b;belfryHazardRoom=b.room;belfryHazardKey=b.hazardVersion;bellHazardKey=b.boss.hazardVersion;
        int[] pixels=new int[320*240];
        for(int y=49;y<217;y++)for(int x=16;x<304;x++)if(b.hazardAt(x,y)){
            boolean edge=!b.hazardAt(x-1,y)||!b.hazardAt(x+1,y)||!b.hazardAt(x,y-1)||!b.hazardAt(x,y+1);
            if(edge||(x+y)%4==0)pixels[y*320+x]=edge?0xffffd78c:0xff963e3b;
        }
        belfryWarned=Image.createRGBImage(pixels,320,240,true);
    }
    public void belfry(Graphics g,World w){
        Belfry b=w.belfry;boolean ceilingBell=!b.boss.fallenBell&&b.boss.state!=World.TRANSITION;
        int key=b.room*8+b.fireMask+(b.room==4&&!ceilingBell?40:0);
        if(belfryFloor==null||key!=belfryFloorKey){belfryFloor=BelfryArt.create(b.room,b.fireMask,ceilingBell);belfryFloorKey=key;}
        g.drawImage(belfryFloor,0,0,Graphics.TOP|Graphics.LEFT);BelfryArt.ambient(g,b,w.clock);
        if(b.room==3)bridge(g,b.guardianDefeated&&b.fireMask==7);
        belfryTelegraph(b);g.drawImage(belfryWarned,0,0,Graphics.TOP|Graphics.LEFT);
        // Draw the bell between actors by their planted feet, preserving both courtyard paths.
        int count=0;
        for(int i=0;i<b.enemies.length;i++)if(b.enemies[i].active&&b.enemies[i].hp>0||b.enemies[i].hp==0&&b.enemies[i].deathTime<Belfry.ENEMY_CORPSE_TIME){chapterActors[count]=i;chapterDepth[count++]=b.enemies[i].y;}
        chapterActors[count]=2;chapterDepth[count++]=w.py;
        if(b.room==4){chapterActors[count]=3;chapterDepth[count++]=b.boss.y;}
        if(b.room==2||b.room==4&&(b.boss.fallenBell||b.boss.state==World.TRANSITION)){chapterActors[count]=4;chapterDepth[count++]=b.room==2?142:138;}
        for(int i=1;i<count;i++)for(int j=i;j>0&&chapterDepth[j]<chapterDepth[j-1];j--){int a=chapterActors[j];chapterActors[j]=chapterActors[j-1];chapterActors[j-1]=a;float d=chapterDepth[j];chapterDepth[j]=chapterDepth[j-1];chapterDepth[j-1]=d;}
        for(int i=0;i<count;i++){
            int a=chapterActors[i];if(a<2)BelfryArt.guard(g,b.enemies[a],w.clock);else if(a==2)hero(g,w);else if(a==3)BellboundArt.draw(g,b.boss,w.clock,w.mode==World.DAWN?w.deathTime:0);else if(b.room==2)BelfryArt.courtBell(g);else BellboundArt.fallenBell(g,b.boss);
        }
        chapterHud(g,w);
        if(b.room==0&&b.entranceQuoteTime>0&&w.mode==World.BELFRY&&b.wakeTime>=Balance.CAMP_WAKE_TIME){
            panel(g,43,171,234,36);
            center(g,"THE BELLS STILL TOLL.",182,0xe9ce9d,1);
            center(g,"BUT THIS PLACE HAS NEVER KNOWN DAWN.",195,0xc4c0bd,1);
        }else if(b.noticeTime>0&&w.mode==World.BELFRY)chapterNotice(g,b);
        if(w.mode==World.BELFRY_LOSE)fade(g,w.fadeOutTime(),Balance.FADE_OUT_TIME);
        else if(w.mode==World.BELFRY&&b.wakeTime<Balance.CAMP_WAKE_TIME)fade(g,Math.max(0,Balance.FADE_IN_TIME-b.wakeTime),Balance.FADE_IN_TIME);
        if(w.mode==World.DAWN){
            panel(g,30,76,260,118);center(g,"THE BELLS FALL SILENT",90,0xe6c890,2);
            center(g,"FIRST DAWN SHARD",113,0xf5dba4,1);
            center(g,"THE RED LIGHT MOVES.",133,0xd6a291,1);
            center(g,"SOMETHING IN HEART OF DAWN BREATHES.",147,0xb7afba,1);
            if(w.deathTime>=3000)center(g,"J / CENTER - RETURN TO THE REFUGE",174,0xddd0b6,1);
        }
    }
    private void bridge(Graphics g,boolean open){
        if(open){for(int i=0;i<4;i++){rect(g,0x6b6261,274+i*7,133,6,24);rect(g,0xa0927b,275+i*7,133,5,1);}g.setColor(0x969085);g.drawLine(273,130,303,130);g.drawLine(273,160,303,160);}
        else{rect(g,0x0a1220,277,127,26,36);for(int i=0;i<4;i++)BelfryArt.chain(g,280+i*6,125,280+i*6,163,0x867363);}
    }
    private void chapterHud(Graphics g,World w){
        Belfry b=w.belfry;rect(g,0x0b111b,0,0,320,26);
        text(g,"KNIGHT",7,4,0xd8d8d2,1);rect(g,0x4e2d36,7,14,76,6);rect(g,0xc95f60,7,14,w.hp*76/Balance.PLAYER_HP,6);
        text(g,"FLASK "+w.potions,98,4,0xddba73,1);text(g,"ROLL",155,4,0x9ac9c9,1);
        rect(g,0x293b45,155,14,40,4);rect(g,0x7fdad0,155,14,40*(Balance.ROLL_COOLDOWN-w.dodgeCd)/Balance.ROLL_COOLDOWN,4);
        for(int i=0;i<3;i++){rect(g,(b.fireMask&(1<<i))!=0?0xe7ad5e:0x49414a,211+i*9,13,5,7);}
        text(g,"P:PAUSE",276,4,0x938e9b,1);text(g,(b.room+1)+" / 5",276,15,0xc5b4a5,1);
        rect(g,0x0b111b,0,216,320,24);
        if(b.room==4){
            text(g,b.boss.phase==2?"II THE BELLBOUND":"THE BELLBOUND",10,220,0xd3b9a2,1);
            rect(g,0x452d38,10,232,300,4);rect(g,0xc07566,10,232,300*b.boss.hp/Bellbound.MAX_HP,4);
            if(b.boss.state==World.WARNING||b.boss.state==World.ACTIVE){String[] moves={"CHAIN SWEEP","BELL CRUSH","FUNERAL TOLL"};center(g,moves[b.boss.attack],29,0xffd78c,1);}
            else if(b.boss.state==World.RECOVER&&!b.boss.dead)center(g,"OPEN - THE MACE IS STUCK",29,0xc5d4b3,1);
            else if(b.boss.state==World.TRANSITION)center(g,"THE BELL IS FALLING",29,0xf1bd8a,1);
        }else{
            String[] names={"ASHEN STEPS","UNLIT CORRIDOR","FALLEN BELL COURT","CHAIN CHAMBER"};
            text(g,names[b.room],10,220,0xc3bbaf,1);
            String prompt=b.wakeTime<Balance.CAMP_WAKE_TIME?"YOU WAKE BESIDE THE EMBER":b.fireIndex()>=0&&World.length(w.px-b.torchX(),w.py-b.torchY())<30?"J - LIGHT / READ THE BRAZIER":b.room==2&&!b.flaskTaken&&World.length(w.px-72,w.py-185)<26?"J - TAKE THE FLASK":b.room==2&&World.length(w.px-60,w.py-110)<27?"J - READ THE INSCRIPTION":b.room==3&&b.guardianDefeated&&World.length(w.px-242,w.py-177)<33?"J - REST / SET CHECKPOINT":World.length(w.px-291,w.py-145)<30?b.exitReady()?"J - CONTINUE THE JOURNEY":"THE WAY IS SEALED":World.length(w.px-29,w.py-145)<28?"J - RETURN":"J - INTERACT / SLASH   L - HEAL";
            center(g,prompt,231,0xddc49f,1);
        }
    }
    private void chapterNotice(Graphics g,Belfry b){
        String a="",z="";
        if(b.notice==1){a=b.room==1?"THE SCRATCHES ALL POINT TO THE EXIT.":b.room==2?"THE KEEPERS CHAINED THEMSELVES TO THE BELLS.":"THEY WAITED FOR A DAWN THAT NEVER CAME.";z="THE FIRE REVEALS AND SLOWS THE HIDDEN ARMOUR.";}
        else if(b.notice==2){a="ONE FLASK RECOVERED FROM THE ASH.";z="THE LONGER PATH REMEMBERS THE LIVING.";}
        else if(b.notice==3){a="WE CHAINED OURSELVES TO CALL THE DAWN.";z="NO ONE REMAINED TO RELEASE US.";}
        else if(b.notice==4){a="THREE FIRES BURN. THE STONE BRIDGE OPENS.";z="REST AT THE EMBER BEFORE THE FINAL CLIMB.";}
        else if(b.notice==5){a="CHECKPOINT KINDLED. HEALTH AND FLASKS RESTORED.";z="THE BELLS ARE LOUDEST ABOVE YOU.";}
        else if(b.notice==6){a="THE WAY REMAINS SEALED.";z="LIGHT THE BRAZIERS. DEFEAT THE KEEPERS.";}
        else if(b.notice==7){a="THE FIRST DAWN SHARD IS YOURS.";z="HEART OF DAWN IS ALIVE.";}
        if(a.length()>0){panel(g,12,28,296,34);center(g,a,36,0xe7c695,1);center(g,z,49,0xaeaab7,1);}
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
        center(g,"THE LAST EMBER / THE ASHEN BELFRY",80,0x8d8597,1);
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
            g.drawImage(floor,0,0,Graphics.TOP|Graphics.LEFT);ArenaArt.ambient(g,w.clock);
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
        String[] rows={"MOVE: D-PAD OR W A S D","DIAGONALS: TWO KEYS / 1 3 7 9","SLASH: CENTER / J / 5","ROLL: DOUBLE-TAP A MOVE KEY","HEAL: L / 2 - 3 FLASKS","PAUSE: P / LEFT SOFTKEY","SOUND: M    EXIT: Q ON TITLE","","THE OATHKEEPER IS NEARLY UNBEATABLE.","AMBER MARKS THE WHOLE DANGER ZONE.","CYAN ROLL GLOW MEANS INVINCIBLE.","WIN OR FALL - WAKE AT THE BONFIRE.","J: REST / LIGHT / READ / USE ARCH.","THREE BRAZIERS OPEN THE BELFRY BRIDGE."};
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
        if(HeroSprites.unconscious(w)){heroSprites.draw(g,w,facing(w.fx,w.fy));return;}
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
