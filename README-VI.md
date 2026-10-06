# AshGate: The Black Oath — Nokia E72, v1.9.0

Game hành động Java ME, khung hình ngang 320 × 240, chương mở đầu **The Last Ember**. Sau phần giới thiệu và cảnh bước qua cổng, hiệp sĩ đối đầu với **Black Oathkeeper**, kẻ giữ ngọn lửa cuối cùng. Chiến thắng mở đường tới Heart of Dawn để khôi phục ánh sáng cho thế giới hậu tận thế.

Đồ họa dùng pixel art theo ảnh hiệp sĩ tham khảo: giáp thép tím xám, bóng đậm, chi tiết kim loại và tỷ lệ cơ thể rõ. Nhân vật và boss dùng atlas hoạt ảnh; cảnh đổ nát, mặt đất nứt vỡ, sương và ánh lửa được vẽ bằng mã Java ME. Đây là cách thể hiện chất liệu và không khí dark fantasy ở độ phân giải nhỏ của E72. Không cần mạng, tài khoản hoặc tải thêm tài nguyên khi chơi.

## Cài trên Nokia E72

Gói cài nhanh là `dist/AshGate-E72-1.9.0.zip`, gồm JAR, JAD và hướng dẫn. Giải nén trên máy tính trước khi chép vào điện thoại. Gói ZIP điện thoại không chứa trình giả lập hoặc mã nguồn.

1. Chép **cả `dist/AshGate.jar` và `dist/AshGate.jad`** vào cùng một thư mục trong điện thoại/thẻ nhớ bằng USB hoặc Bluetooth.
2. Trong trình quản lý tệp của điện thoại, mở `AshGate.jar` để cài. Có thể thử mở `AshGate.jad` nếu trình cài cần mô tả ứng dụng.
3. Mở **AshGate** trong danh sách ứng dụng. Dùng lên/xuống để chọn **BEGIN JOURNEY**, **CONTROLS**, **SOUND** hoặc **EXIT**; nhấn phím giữa D-pad hoặc **J** để xác nhận. **H** mở nhanh hướng dẫn.
4. Đây là bản JAR chưa ký chứng chỉ. Máy có thể hỏi xác nhận nhà phát triển chưa được xác minh. Chỉ cho phép cài nếu bạn đã chủ động tải đúng gói này.

Không cần Python hoặc Java trên máy tính nếu chỉ chép bản JAR sang E72. Đây là MIDlet Java ME, không phải ứng dụng Symbian SIS/SISX.

## Điều khiển

| Hành động | Phím ưu tiên trên E72 | Phím bổ sung |
|---|---|---|
| Di chuyển | D-pad hoặc W/A/S/D | 4/6/8: trái/phải/xuống |
| Đi chéo | Giữ hai hướng W/A/S/D | 1/3/7/9: trên trái/trên phải/dưới trái/dưới phải |
| Chém một lần | Phím giữa D-pad hoặc J | 5 |
| Lăn né | Nhấn–thả–nhấn cùng hướng D-pad hoặc W/A/S/D | Nhấn hai lần 1/3/7/9 để né chéo |
| Uống bình máu | L | 2 |
| Tạm dừng/tiếp tục | P | Phím mềm trái |
| Bật/tắt âm thanh | M | — |
| Xem hướng dẫn | H ở màn hình đầu | — |
| Về màn hình đầu | Q | Phím mềm phải |
| Thoát ứng dụng | Q ở màn hình đầu | Phím mềm phải |
| Chơi lại | Phím giữa hoặc J sau khi hiện bảng kết quả | 5 |

**Số 2 được dành cho hồi máu**, không phải di chuyển lên. Ưu tiên D-pad + J/L hoặc W/A/S/D + J/L trên bàn phím QWERTY. Nếu thiết bị không nhận tổ hợp hai hướng, dùng 1/3/7/9 để đi chéo bằng một phím. Ánh xạ phím đặc thù firmware có thể chỉnh trong `Game.map()`.

## Bộ hoạt ảnh mới: 144 khung

Bản 1.9.0 dùng sáu chuỗi hoạt ảnh: đứng/thở, lăn né, uống bình, chém, di chuyển và chết. Mỗi chuỗi có sáu frame theo bốn hướng **xuống → trái → phải → lên**, phát từ trái sang phải. Di chuyển dùng `assets/knight-sheets/knight_walk_classic.png`: chu kỳ bước đã sửa được chuyển về bảng màu giáp tím xám cũ bằng AI.

Đứng/thở, chém và uống bình dùng các nguồn AI `knight_idle_matched.png`, `knight_attack_matched.png` và `knight_heal_matched.png` trong cùng thư mục, thống nhất giáp, màu sắc và tỷ lệ với nhân vật walk. Hoạt ảnh chém giữ kiếm ở tay phải; uống bình dùng tay trái trong khi tay phải giữ kiếm. Lăn/chết giữ các sheet có sẵn trong dự án. Ảnh `168-Knight.webp` là tham khảo về phong cách giáp, không được nhúng vào game.

Di chuyển dùng 95 ms/khung, đứng dùng 170 ms/khung và lặp lại. Lăn trải sáu khung trong 360 ms, uống trong 1000 ms. Chém dùng cột 0–1 trong 150 ms chuẩn bị, 2–3 trong 100 ms gây sát thương, 4–5 trong 330 ms hồi phục, tổng cộng 580 ms. Chết phát một lần với 120 ms/khung, giữ khung cuối; sau 720 ms hiện bảng thất bại và cho phép chơi lại. Chơi lại xóa thời gian chết và khôi phục toàn bộ trạng thái trận đấu.

Ảnh nguồn và `frames.json` nằm trong `assets/knight-sheets/`. Công cụ `tools/pack_hero_sprites.py` cắt theo tọa độ và điểm neo của từng frame trong JSON, thu nhỏ nearest-neighbor rồi đặt vào chung điểm neo `(40, 52)` trong ô atlas. Hai sheet lăn/chết cũ giữ khoảng cách 50 px và tỷ lệ 0,26. Bốn sheet walk/idle/attack/heal mới đều có kích thước 1536 × 1024; mỗi sheet dùng 24 vùng cắt và điểm neo riêng, với tỷ lệ theo hướng được lưu trong metadata. Sheet attack dùng tọa độ cột riêng theo từng hàng để giữ đầy đủ kiếm khi vung ra ngoài vị trí thân. SHA-256 của bốn nguồn mới được kiểm tra trước khi đóng gói.

Bốn nguồn mới có nền đen. Chế độ `black_matte` loại nền đen và vùng tối trung tính nối với nền bằng flood-fill, giữ các chi tiết đen khép kín như khe mặt nạ. Atlas cuối dùng alpha 0 hoặc 255. Bốn sheet mới đã có thứ tự hướng đúng và dùng `mirror_profiles: false`. Sheet chết cũ vẫn phản chiếu hai hàng ngang khi đóng gói để giữ hướng nhất quán.

Atlas `res/hero-atlas.png` có 144 ô 80 × 64, kích thước 480 × 1536, nền trong suốt nhị phân phù hợp MIDP. Phần đệm ngang giữ đủ lưỡi kiếm trong các pha chém; điểm chân và vị trí thân trên màn hình được giữ ổn định. Chỉ atlas được nhúng trong JAR; các sheet lớn được giữ trong mã nguồn. Tạo lại bằng `python3 tools/pack_hero_sprites.py` (cần Pillow), rồi `python3 build.py` và kiểm tra bằng `python3 test.py`.

Hitbox và thông số chiến đấu không phụ thuộc pixel sprite. Vẫn nhấn hai lần hướng trong 280 ms để lăn và miễn sát thương suốt động tác lăn. Boss là bộ xương trong giáp tím xám vỡ, lộ sọ, xương sườn, cột sống và các khớp xương màu ngà. Thân đứng cao 88 px, gấp đôi nhân vật 44 px; nguồn AI `assets/boss-sheets/boss_skeleton.png` có 32 dáng theo bốn hướng **xuống → trái → phải → lên**. Mỗi hàng có đứng, bốn pha bước, chuẩn bị kiếm, chém và ngã. Khi trúng đòn thân giật ngang 2 px; giai đoạn II có vòng sáng dưới chân. Điểm chân giữ nguyên trong mọi dáng, kể cả ngã. Boss được giới hạn ở phần đấu trường đủ thấp để sọ nằm dưới HUD. Xem ảnh xuất từ bản hiện tại: `preview/sprite-actions-0.png`, `preview/death-animation-5.png`, `preview/directions-0.png`, `preview/boss-skeleton-directions.png` và `preview/boss-skeleton-actions.png`. Kiểm tra render cả 144 khung nhân vật và 32 dáng boss qua MicroEmulator; chưa thử trên máy E72 thật.

`tools/pack_boss_sprites.py` tách các hình riêng theo alpha từ hai nguồn trong suốt: 25 dáng lấy từ `boss_skeleton.png`, bảy khung bước có kiếm lấy từ bản AI đã sửa `boss_skeleton_walk_fixed.png` trong cùng thư mục. Công cụ thu nhỏ nearest-neighbor và neo đế giày ở `(112, 100)`. Atlas boss `res/boss-atlas.png` có 32 ô 224 × 112, kích thước 1792 × 448, alpha nhị phân và phần đệm giữ đủ lưỡi kiếm. Hai dáng chuẩn bị/chém sang trái dùng ảnh phía phải phản chiếu để đúng hướng. Metadata ghi nguồn của từng khung, vùng cắt, tỷ lệ và SHA-256 của cả hai ảnh trong `assets/boss-sheets/frames.json`. Tạo lại bằng `python3 tools/pack_boss_sprites.py`. `SkeletonBossArt` là phương án vẽ bằng mã nếu tài nguyên boss không tải được.

## Màn hình chính và giới thiệu cốt truyện

Màn hình chính có nền tàn tích, ánh lửa và sương mù chuyển động. Lên/xuống hoặc W/S chuyển lựa chọn; J/phím giữa xác nhận. Có bắt đầu hành trình, điều khiển, bật/tắt âm thanh và thoát.

Xác nhận BEGIN JOURNEY sẽ mở ba cảnh pixel có chuyển mờ qua đen, mỗi cảnh 4,5 giây (tổng khoảng 13,5 giây):

1. **The Fallen Kingdom**: mặt trời đã tắt, vương quốc hóa tro; một lời thề bị phá vỡ trói cả thế giới vào cái chết.
2. **The Black Oath**: Oathkeeper giữ ngọn lửa cuối cùng. Sau cổng của hắn là Heart of Dawn, hy vọng khôi phục ánh sáng.
3. **The Last Knight**: hiệp sĩ cuối cùng lên đường giành lại bình minh và cứu thế giới.

J/phím giữa bỏ qua toàn bộ giới thiệu và chuyển tới cảnh bước qua cửa đá; P tạm dừng; Q trở về menu. Giới thiệu tự kết thúc rồi vào trận boss. Trong giới thiệu, người chơi không bị đánh hoặc tiêu hao bình máu. Chiến thắng thông báo con đường tới Heart of Dawn đã mở; bản này kết thúc ở trận boss mở đầu. Chơi lại sau thắng/thua vào cảnh cửa đá trực tiếp, không lặp đoạn cốt truyện. Chọn hành trình mới từ menu sẽ phát lại giới thiệu từ đầu.

Chữ trong game vẫn là tiếng Anh ASCII; phần cốt truyện tiếng Việt được diễn giải ở trên. Có thể chỉnh thời lượng trong `Balance.STORY_SCENE_TIME` và `STORY_SCENES`.

## Cách chơi

Đi qua cổng đá để bắt đầu trận. Boss có chém hình quạt, bổ kiếm đường hẹp, lao thẳng và đập đất hình tròn. Vùng gạch chéo đỏ có viền vàng là toàn bộ phạm vi nguy hiểm trên mặt đất. Vùng thân tính va chạm của hiệp sĩ là vòng tròn bán kính 5 pixel quanh vị trí chân; phần đầu/kiếm trang trí không phải hitbox.

Boss khóa hướng khi cảnh báo xuất hiện. Đòn lao chỉ gây sát thương khi boss đi qua phần đường tương ứng; vùng cảnh báo hiển thị toàn bộ đường lao. Các đòn còn lại kiểm tra đúng hình vùng cảnh báo. Đập đất có sát thương trên toàn hình tròn khi đòn bắt đầu; vòng lan tỏa sáng là hiệu ứng, không phải một vòng rỗng có thể đứng trong tâm.

Để lăn né, nhấn rồi thả một phím di chuyển, sau đó nhấn lại cùng hướng trong tối đa **280 ms**, ví dụ phải → thả → phải. Hướng lăn là hướng của phím được nhấn hai lần. Nhấn hai lần phím 1/3/7/9 sẽ né theo đường chéo. Giữ phím hoặc sự kiện tự lặp của bàn phím chỉ di chuyển, không lăn. Đổi hướng giữa hai lần nhấn không kích hoạt né. K, Space và 0 không còn là phím né.

Lăn chỉ kích hoạt khi nhân vật rảnh và thanh né đã hồi đầy; không hủy chém hoặc uống máu. Cặp nhấn không hợp lệ được bỏ qua, không xếp hàng để lăn sau. Tạm dừng, chuyển màn hình và đưa ứng dụng ra nền sẽ xóa lần nhấn trước. Có thể chỉnh độ nhạy trong `Balance.DOUBLE_TAP_TIME`.

Lăn né ngắn khoảng 32,4 pixel. Nhân vật miễn toàn bộ sát thương trong suốt hành động lăn, từ lúc bắt đầu đến khi kết thúc ở 360 ms. Viền xanh ngọc và trạng thái IMMUNE duy trì suốt động tác. Miễn sát thương kết thúc ngay khi nhân vật trở lại trạng thái đứng/di chuyển, không kéo dài qua thời gian chờ né. Thanh ROLL đầy thì mới lăn tiếp được. Sau khi trúng đòn có 650 ms miễn sát thương.

Chém: chuẩn bị 150 ms → gây sát thương 100 ms → hồi phục 330 ms. Giữ phím không tự chém liên tục; phải thả rồi nhấn lại. Chém trúng gây 24 sát thương, mỗi nhát chỉ trúng boss một lần. Boss có 720 HP, cần 30 nhát trúng. Boss vẫn có thể bị chém khi đang chuẩn bị, nhưng thời gian có chữ **OPEN** là cơ hội an toàn hơn để phản công.

Có 3 bình máu. Bình bị tiêu thụ khi bắt đầu uống, hồi 40 HP sau khoảng một giây và không vượt 100 HP. Bị đánh sẽ ngắt uống và mất bình đã dùng. Trong lúc uống không thể di chuyển, chém hoặc né. Không thể uống khi máu đầy.

| Đòn boss | Cảnh báo giai đoạn I / II | Sát thương | Hồi phục I / II |
|---|---:|---:|---:|
| Chém ngang | 800 / 680 ms | 22 | 850 / 750 ms |
| Bổ kiếm | 900 / 780 ms | 32 | 950 / 850 ms |
| Lao tới | 1000 / 880 ms | 25 | 1000 / 900 ms |
| Đập đất | 1000 / 880 ms | 28 | 1100 / 1000 ms |

Boss vào giai đoạn II khi dưới 360 HP, sau một khoảng chuyển trạng thái. Tốc độ tiếp cận tăng từ 24 lên 29 pixel/giây, nhịp chọn đòn nhanh hơn và chuỗi đòn thay đổi. Cảnh báo không bị bỏ qua. Không có sát thương do chỉ chạm thân boss.

## Chạy thử trên máy tính

Có sẵn MicroEmulator 2.0.4 trong `tools/`. Cần Java desktop 8 trở lên; Java 8 được ưu tiên trên Windows và đã dùng để kiểm tra bản này.

- Windows: mở `play-desktop.bat`. Trình chạy tìm Java cài trực tiếp và tránh phụ thuộc vào đường dẫn chuyển tiếp Java bị lỗi.
- Linux/macOS có giao diện đồ họa: `sh play-desktop.sh`.
- Lệnh trực tiếp: `java -jar tools/microemulator.jar --resizableDevice 320 240 dist/AshGate.jad`.

Trình giả lập mở màn hình 320 × 240. Dùng phím mũi tên; phím Fire tùy ánh xạ của giả lập, có thể dùng phím 5. Các phím chữ phụ thuộc ánh xạ bàn phím của giả lập; sơ đồ số là phương án thay thế. Bản điện thoại vẫn nhận phím chữ thông qua Canvas.

Có thể chọn Java bằng biến môi trường `JAVA` (đường dẫn đầy đủ tới `java.exe` hoặc `java`) hoặc `JAVA_HOME` (thư mục cài Java). `JAVA` được ưu tiên khi đặt cả hai biến. Ví dụ PowerShell: `$env:JAVA = 'C:\Program Files\Java\jre1.8.0_503\bin\java.exe'`, sau đó chạy script. Các script báo lỗi rõ nếu thiếu Java hoặc thiếu gói game.

## Biên dịch lại

Cần Java 8+ và Python 3. Các JAR công cụ đã có trong `tools/`.

```sh
python3 build.py
python3 test.py
```

Windows: mở `build.bat`; script tự tìm Python 3 đã cài và có thể dùng runtime đi kèm Codex nếu có. Không bắt buộc có lệnh `py`. Nếu Python đã có trong PATH, dùng `python build.py` và `python test.py`; cũng có thể gọi trực tiếp `python.exe`. Biến `ASHGATE_PYTHON` cho phép chọn đường dẫn Python cho `build.bat`.

`build.py` dùng Eclipse ECJ để tạo class version 45, biên dịch với CLDC/MIDP bootclasspath, sau đó ProGuard `-microedition` tạo StackMap dành cho CLDC. Kết quả nằm trong `dist/`; kích thước JAR trong JAD được cập nhật tự động. Manifest là ASCII không BOM.

## Cấu trúc mã nguồn

- `src/Balance.java`: cấu hình máu, sát thương, tốc độ, phạm vi và thời gian.
- `src/World.java`: trạng thái trận đấu, hitbox, boss AI, hồi máu, né, thắng/thua, reset.
- `src/HeroSprites.java`: tải atlas và ánh xạ 144 khung sprite vào sáu trạng thái.
- `src/BossSprites.java`: tải atlas 32 dáng boss, chọn hướng/trạng thái, giữ điểm chân và vẽ giật người/vòng sáng giai đoạn II.
- `src/KnightArt.java`: bộ giáp vẽ bằng mã và các lời gọi chuyển tới bộ khung boss.
- `src/SkeletonBossArt.java`: phương án boss bộ xương trong giáp vẽ bằng mã, cao 88 px, với điểm tay dùng chung với kiếm.
- `src/Art.java`: đồ họa pixel, hoạt ảnh theo trạng thái, font bitmap, cảnh báo và HUD.
- `src/Game.java`: nhận phím, tách nhấn mới khỏi giữ phím, vòng lặp, tạm dừng, âm thanh.
- `src/AshGate.java`: vòng đời MIDlet.
- `tests/`: kiểm tra mô hình chiến đấu, đầu vào Canvas và render thật bằng MicroEmulator.
- `preview/`: ảnh 320 × 240 được xuất từ renderer Java ME của bản JAR.
- `TEST-RESULTS.txt`: kết quả kiểm tra của bản bàn giao.

Mô phỏng dùng bước 33 ms (~30 lần/giây); thời gian thực tế được lượng tử theo bước này. Bản chậm sẽ không nhảy vượt toàn bộ một đòn để bù khung hình bị mất. Hình nền và lớp cảnh báo được cache; không cần tải sprite bên ngoài. Hiệu ứng âm thanh dùng `Manager.playTone` và tự bỏ qua khi máy không hỗ trợ.

## Đã kiểm tra và giới hạn

- 51 kiểm tra chiến đấu tự động: hướng/range, một hit mỗi đòn, bốn đòn boss, cảnh báo không gây sát thương, né, cooldown, hồi máu và ngắt hồi, giới hạn đấu trường, chuyển giai đoạn, thắng/thua/reset và xóa thời gian giới thiệu cũ khi chơi lại.
- 7 kiểm tra đầu vào trên Canvas MicroEmulator: bắt đầu, giữ phím chém, nhấn lại, tạm dừng, đi chéo, về đầu và hướng dẫn.
- 15 kiểm tra menu/cốt truyện: chọn menu, âm thanh, tạm dừng, bỏ qua, tự chuyển ba cảnh, hủy/vào lại và chơi lại.
- Kiểm tra miễn sát thương cả đầu/cuối động tác lăn với bốn đòn boss, và nhận lại sát thương sau khi động tác kết thúc.
- 16 kiểm tra nhấn hai lần: một lần không né, đủ hướng, chéo, cửa sổ 280 ms, giữ/tự lặp không né, cooldown, khóa khi chém/uống, tạm dừng/ra nền và loại bỏ phím né cũ.
- Kiểm tra các dáng bốn hướng khác nhau, chu kỳ bước thay đổi và render các trạng thái chém, lăn, uống và chịu đòn ở cả bốn hướng.
- Kiểm tra 32 dáng boss tải từ atlas thật: thân đứng 88 px so với nhân vật 44 px, xương nhìn rõ ở bốn hướng, alpha nhị phân, không cắt kiếm và kiếm vẫn hiện trong mọi khung bước, đế giày ổn định khi bước/chém/ngã; giật người và giai đoạn II vẫn dùng atlas. Kiểm tra khởi tạo, tiếp cận và lao về mép bắc ở cả hai giai đoạn để boss không lấn HUD; bộ khung dự phòng cũng được render riêng.
- Xuất thành công 13 khung hình bằng implementation MIDP của MicroEmulator và kiểm tra bố cục trực quan.
- Kiểm tra class version, StackMap, manifest không BOM và kích thước JAD khớp JAR.

**Chưa chạy trực tiếp trên Nokia E72 thật, chưa đo FPS/âm thanh/độ nhận phím trên thiết bị.** Việc chạy giao diện giả lập bằng tay trên máy tính của người dùng cũng chưa được xác nhận; các kiểm tra ở đây chạy bằng Java desktop không có cửa sổ. Nếu máy báo lỗi cài, gửi nguyên văn lỗi và xác nhận đã chép đúng cặp JAR/JAD cùng phiên bản.

Đây là chương mở đầu chơi được, gồm giới thiệu và một trận boss. Không có lưu trận; thoát hoặc chơi lại sẽ bắt đầu trận mới. Giao diện trong game dùng tiếng Anh ASCII để giữ chữ rõ và ổn định ở 320 × 240.

## Tài liệu kỹ thuật

- Oracle Java ME: https://docs.oracle.com/javame/mobile.html
- Oracle quy trình biên dịch và preverify: https://docs.oracle.com/javame/dev-tools/jme-sdk-3.0-mac/UserGuide-html/z400007747176.html
- Guardsquare cấu hình Java ME: https://www.guardsquare.com/manual/configuration/examples
- MicroEmulator: https://github.com/tisoft/microemu
- Nokia E72 Data Sheet (tài liệu Nokia, bản lưu): https://images-eu.ssl-images-amazon.com/images/G/03/electronics/Nokia_E72_Data_Sheet._V185028282_.pdf — xác nhận màn hình QVGA 320 × 240. Gói game nhắm CLDC 1.1 / MIDP 2.0; kết quả chạy trên thiết bị thật vẫn cần kiểm tra trực tiếp.
