# AshGate: The Black Oath — Nokia E72, v1.13.0

Game hành động Java ME, khung hình ngang 320 × 240. Sau phần giới thiệu, hiệp sĩ đối đầu với **Black Oathkeeper đúng một lần trong mỗi hành trình**. Dù thắng hay thua, hiệp sĩ đều tỉnh dậy bên lửa trại và được đi tiếp tới **Tháp Chuông Tro Tàn — The Ashen Belfry**, năm khu nối liền với ba đài lửa, lính canh, một lính tinh nhuệ và boss mới **Kẻ Kéo Chuông — The Bellbound**. Mảnh Bình Minh đầu tiên đưa hành trình cứu thế giới tới gần Heart of Dawn hơn.

Đồ họa dùng pixel art theo ảnh hiệp sĩ tham khảo: giáp thép tím xám, bóng đậm, chi tiết kim loại và tỷ lệ cơ thể rõ. Nhân vật, lính và boss dùng atlas hoạt ảnh; tầng hầm, tháp chuông phủ tro, đài lửa, chuông đổ cùng xích và chùy Bellbound được vẽ bằng mã Java ME. Không cần mạng, tài khoản hoặc tải thêm tài nguyên khi chơi.

## Cài trên Nokia E72

Gói cài nhanh là `dist/AshGate-E72-1.13.0.zip`, gồm JAR, JAD và hướng dẫn. Giải nén trên máy tính trước khi chép vào điện thoại. Gói ZIP điện thoại không chứa trình giả lập hoặc mã nguồn.

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
| Tương tác với lửa, cổng, đuốc hoặc vật phẩm | Phím giữa D-pad hoặc J khi đứng gần | 5 |

**Số 2 được dành cho hồi máu**, không phải di chuyển lên. Ưu tiên D-pad + J/L hoặc W/A/S/D + J/L trên bàn phím QWERTY. Nếu thiết bị không nhận tổ hợp hai hướng, dùng 1/3/7/9 để đi chéo bằng một phím. Ánh xạ phím đặc thù firmware có thể chỉnh trong `Game.map()`.

## Hoạt ảnh hiệp sĩ

Nhân vật dùng sáu chuỗi hoạt ảnh: đứng/thở, lăn né, uống bình, chém, di chuyển và chết. Mỗi chuỗi có sáu frame theo bốn hướng **xuống → trái → phải → lên**, phát từ trái sang phải. Di chuyển dùng `assets/knight-sheets/knight_walk_classic.png`: chu kỳ bước đã sửa được chuyển về bảng màu giáp tím xám cũ bằng AI.

Đứng/thở, chém và uống bình dùng các nguồn AI `knight_idle_matched.png`, `knight_attack_matched.png` và `knight_heal_matched.png` trong cùng thư mục, thống nhất giáp, màu sắc và tỷ lệ với nhân vật walk. Hoạt ảnh chém giữ kiếm ở tay phải; uống bình dùng tay trái trong khi tay phải giữ kiếm. Lăn/chết giữ các sheet có sẵn trong dự án. Ảnh `168-Knight.webp` là tham khảo về phong cách giáp, không được nhúng vào game.

Di chuyển dùng 95 ms/khung, đứng dùng 170 ms/khung và lặp lại. Lăn trải sáu khung trong 360 ms, uống trong 1000 ms. Chém dùng cột 0–1 trong 150 ms chuẩn bị, 2–3 trong 100 ms gây sát thương, 4–5 trong 330 ms hồi phục, tổng cộng 580 ms. Khi thua, hoạt ảnh ngã phát một lần với 120 ms/khung trong 720 ms, rồi giữ khung cuối khi cảnh mờ sang đen. Khi thắng, hiệp sĩ đứng bên boss đã ngã khoảng 5 giây rồi kiệt sức, phát cùng hoạt ảnh ngã và chuyển cảnh. Hiệp sĩ tỉnh dậy bên lửa trại; chuỗi tỉnh dậy dùng các khung ngã theo thứ tự ngược để trở lại tư thế đứng.

Ảnh nguồn và `frames.json` nằm trong `assets/knight-sheets/`. Công cụ `tools/pack_hero_sprites.py` cắt theo tọa độ và điểm neo của từng frame trong JSON, thu nhỏ nearest-neighbor rồi đặt vào chung điểm neo `(40, 52)` trong ô atlas. Hai sheet lăn/chết cũ giữ khoảng cách 50 px và tỷ lệ 0,26. Bốn sheet walk/idle/attack/heal mới đều có kích thước 1536 × 1024; mỗi sheet dùng 24 vùng cắt và điểm neo riêng, với tỷ lệ theo hướng được lưu trong metadata. Sheet attack dùng tọa độ cột riêng theo từng hàng để giữ đầy đủ kiếm khi vung ra ngoài vị trí thân. SHA-256 của bốn nguồn mới được kiểm tra trước khi đóng gói.

Bốn nguồn mới có nền đen. Chế độ `black_matte` loại nền đen và vùng tối trung tính nối với nền bằng flood-fill, giữ các chi tiết đen khép kín như khe mặt nạ. Atlas cuối dùng alpha 0 hoặc 255. Bốn sheet mới đã có thứ tự hướng đúng và dùng `mirror_profiles: false`. Sheet chết cũ vẫn phản chiếu hai hàng ngang khi đóng gói để giữ hướng nhất quán.

Atlas `res/hero-atlas.png` có 144 ô 80 × 64, kích thước 480 × 1536, nền trong suốt nhị phân phù hợp MIDP. Phần đệm ngang giữ đủ lưỡi kiếm trong các pha chém; điểm chân và vị trí thân trên màn hình được giữ ổn định. Chỉ atlas được nhúng trong JAR; các sheet lớn được giữ trong mã nguồn. Tạo lại bằng `python3 tools/pack_hero_sprites.py` (cần Pillow), rồi `python3 build.py` và kiểm tra bằng `python3 test.py`.

Ngồi nghỉ dùng atlas riêng `res/hero-rest-atlas.png`, gồm 24 ô 80 × 64, kích thước 480 × 256, sáu cột theo bốn hướng **xuống → trái → phải → lên**, điểm neo `(40, 52)`. Nguồn `assets/knight-sheets/knight_rest.png` và metadata `rest-frames.json` trong cùng thư mục được đóng gói bằng `python3 tools/pack_rest_sprites.py`. Sáu dáng chuyển từ đứng sang ngồi; phát ngược để đứng dậy. Atlas hoạt ảnh chiến đấu 144 khung giữ nguyên.

Hitbox và thông số chiến đấu không phụ thuộc pixel sprite. Vẫn nhấn hai lần hướng trong 280 ms để lăn và miễn sát thương suốt động tác lăn. Boss là bộ xương trong giáp tím xám vỡ, lộ sọ, xương sườn, cột sống và các khớp xương màu ngà. Thân đứng cao 88 px, gấp đôi nhân vật 44 px; nguồn AI `assets/boss-sheets/boss_skeleton.png` có 32 dáng theo bốn hướng **xuống → trái → phải → lên**. Mỗi hàng có đứng, bốn pha bước, chuẩn bị kiếm, chém và ngã. Khi trúng đòn thân giật ngang 2 px; giai đoạn II có vòng sáng dưới chân. Điểm chân giữ nguyên trong mọi dáng, kể cả ngã. Boss được giới hạn ở phần đấu trường đủ thấp để sọ nằm dưới HUD. Xem ảnh xuất từ bản hiện tại: `preview/sprite-actions-0.png`, `preview/death-animation-5.png`, `preview/directions-0.png`, `preview/boss-skeleton-directions.png` và `preview/boss-skeleton-actions.png`. Kiểm tra render cả 144 khung nhân vật và 32 dáng boss qua MicroEmulator; chưa thử trên máy E72 thật.

`tools/pack_boss_sprites.py` tách các hình riêng theo alpha từ hai nguồn trong suốt: 25 dáng lấy từ `boss_skeleton.png`, bảy khung bước có kiếm lấy từ bản AI đã sửa `boss_skeleton_walk_fixed.png` trong cùng thư mục. Công cụ thu nhỏ nearest-neighbor và neo đế giày ở `(112, 100)`. Atlas boss `res/boss-atlas.png` có 32 ô 224 × 112, kích thước 1792 × 448, alpha nhị phân và phần đệm giữ đủ lưỡi kiếm. Hai dáng chuẩn bị/chém sang trái dùng ảnh phía phải phản chiếu để đúng hướng. Metadata ghi nguồn của từng khung, vùng cắt, tỷ lệ và SHA-256 của cả hai ảnh trong `assets/boss-sheets/frames.json`. Tạo lại bằng `python3 tools/pack_boss_sprites.py`. `SkeletonBossArt` là phương án vẽ bằng mã nếu tài nguyên boss không tải được.

## Hoạt ảnh lính và boss Tháp Chuông Tro Tàn

Lính gác cầm kiếm và khiên có 32 dáng, thân đứng cao 44 px; lính tinh nhuệ cầm thương có 32 dáng, cao 48 px. Mỗi bộ gồm bốn hướng **xuống → trái → phải → lên**, mỗi hướng có đứng, bốn pha bước, chuẩn bị đánh, ra đòn và ngã. Giáp thép tối, viền đồng cũ và vải rách phân biệt lính với hiệp sĩ giáp tím; lính thương có chùm lông đỏ trên mũ. Vũ khí và khiên đi cùng tư thế, với điểm chân được neo ổn định khi đứng, bước và đánh.

Bellbound dùng 40 dáng thân theo cùng bốn hướng, cao 70 px: đứng, bốn pha bước, chuẩn bị, quét xích, nện chùy, hồi chuông và ngã. Xích cùng chùy lõi chuông được vẽ riêng bằng Java ME để khớp vị trí đòn đánh. Các dáng ngã hạ thân xuống mặt đất thay vì đổi tỷ lệ cả nhân vật. Ngoài ánh sáng đài lửa, lính vẫn dùng hình dáng của từng frame dưới dạng bóng tối; vùng cảnh báo đỏ viền vàng giữ rõ trên nền.

Nguồn PNG nằm trong `assets/belfry-sheets/`. Metadata `guard-frames.json`, `spearman-frames.json` và `bellbound-frames.json` lưu vùng cắt, nguồn từng dáng, tỷ lệ, điểm neo và SHA-256. Hai atlas lính `res/belfry-guard-atlas.png` và `res/belfry-spearman-atlas.png` có kích thước 1024 × 320, ô 128 × 80, điểm chân `(64, 68)`. Atlas thân boss `res/bellbound-atlas.png` có kích thước 1120 × 448, ô 112 × 112, điểm chân `(56, 100)`. Thu nhỏ nearest-neighbor và alpha nhị phân giữ pixel cùng nền trong suốt khi nhúng vào MIDP. Tạo lại bằng `python3 tools/pack_belfry_sprites.py` và `python3 tools/pack_bellbound_sprites.py`; các sheet nguồn lớn được giữ trong mã nguồn.

Các nguồn được chọn, bản sửa vũ khí/bước đi và toàn bộ prompt tạo ảnh được ghi trong [ASSET-NOTES.md](assets/belfry-sheets/ASSET-NOTES.md).

## Phòng boss

Black Oathkeeper đứng trong tầng hầm tối, ẩm và xuống cấp: trần vòm thấp, lối phía sau bị chặn bằng song sắt gỉ, tường loang nước và rêu mốc. Cửa thông gió, ống dẫn cũ, thùng vỡ và đá vụn nằm sát hai bên; năm vũng nước phản chiếu trên nền đá mòn. Hai đèn yếu chỉ soi một phần căn phòng. Nước nhỏ từ trần và tạo gợn vòng trên các vũng nước, cùng lớp sương lạnh mỏng chuyển động phía sau nhân vật.

`ArenaArt` dựng sẵn toàn bộ nền tầng hầm và giữ trong bộ nhớ; mỗi khung chỉ vẽ thêm ánh đèn, giọt nước, gợn nước và sương. Cảnh báo đòn đánh dùng lớp trong suốt riêng, được tạo lại khi vùng nguy hiểm thay đổi, phủ lên cảnh và nằm dưới nhân vật. HUD được vẽ sau cảnh và nhân vật để giữ máu, bình và thanh boss rõ. Ống dẫn, thùng vỡ và đá vụn là trang trí, không thêm vật cản hoặc đổi hitbox chiến đấu. Xem `preview/arena-basement-test.png` và `preview/arena-basement-paused-test.png` được xuất bằng MIDP thật.

## Tỉnh dậy bên lửa trại

Trận boss chuyển cảnh tự động sau cả hai kết quả. Khi hết máu, hiệp sĩ ngã trong 720 ms. Khi boss bị hạ, hiệp sĩ còn đứng khoảng 5 giây rồi ngất trong 720 ms. Tiếp theo, cảnh mờ sang đen trong 900 ms, giữ màn hình đen 550 ms, rồi chuyển tới khu nghỉ và phát chuỗi tỉnh dậy 1800 ms. Cảnh lửa trại hiện dần trong 900 ms đầu của chuỗi tỉnh dậy. Không cần nhấn phím để tiếp tục; chém, né và uống bình bị khóa trong lúc chuyển cảnh hoặc tỉnh dậy.

Khu nghỉ là nơi an toàn để đi lại sau khi tỉnh dậy; hiệp sĩ bắt đầu với 100 HP và 3 bình. Đứng cách lửa trại ở giữa khu nghỉ tối đa 45 pixel và nhấn **J/phím giữa/5** để hồi đầy máu, nạp lại bình ngay và bắt đầu ngồi nghỉ. Nếu đứng trong vành 38 pixel quanh tâm lửa, hiệp sĩ được dịch ngắn ra vành 38 pixel trước khi ngồi để tránh nằm trong ngọn lửa; vị trí phía ngoài vành được giữ nguyên. Hiệp sĩ hạ người trong 720 ms, rồi giữ tư thế ngồi và thở nhẹ. Chuỗi ngồi có bốn hướng, cùng giáp và bảng màu với các hoạt ảnh đứng/đi/chém.

Khi đang ngồi, nhấn **J/phím giữa/5** lần nữa hoặc một hướng **D-pad/W/A/S/D** để đứng dậy trong 720 ms. Vị trí được giữ nguyên trong lúc ngồi xuống và đứng lên; chỉ có thể di chuyển sau khi đứng thẳng. Lối phía trên bên trái mở sang The Ashen Belfry sau cả thắng lẫn thua. Cổng phía trên bên phải đã sập; không thể tái đấu Black Oathkeeper trong hành trình này. Quay lại trại hoặc chết trong tháp không làm xuất hiện lại boss đầu. Chọn **BEGIN JOURNEY** mới sẽ xóa tiến trình và bắt đầu một hành trình có cuộc chạm trán mở đầu riêng. Trong khu nghỉ, J chỉ tương tác; L/2 và nhấn hai lần hướng không uống bình hoặc lăn. **P** tạm dừng/tiếp tục; **Q** trở về menu.

## Tháp Chuông Tro Tàn — The Ashen Belfry

Sau cuộc chạm trán Black Oathkeeper, dù thắng hay thua, tỉnh dậy rồi đi đến lối mở phía trên bên trái khu nghỉ và nhấn **J/phím giữa/5**. Tiếng chuông vang từ một ngọn tháp đã bỏ hoang. Câu dẫn tiếng Anh nằm trong khung nhỏ ở nửa dưới màn hình, dùng chữ bitmap cỡ nhỏ và chỉ hiện **1,8 giây khi lần đầu bước vào phòng**:

> “The bells still toll. But this place has never known dawn.”

Đi ngược từ phòng khác, trở lại từ lửa trại hoặc hồi sinh không làm hiện lại câu dẫn. Bắt đầu hành trình mới sẽ đặt lại lần ghé đầu tiên.

| Khu vực | Khám phá và chiến đấu |
|---|---|
| Bậc thềm phủ tro — Ashen Steps | Cửa sổ vỡ, tro bay, bóng người kéo chuông từ xa. Không có địch trong 3,5 giây đầu, sau đó xuất hiện một lính gác. |
| Hành lang đèn tắt — Unlit Corridor | Đài lửa thứ nhất, hai lính gác xuất hiện lần lượt. Thắp lửa làm hiện các vết cào hướng về cửa thoát. |
| Sân chuông đổ — Fallen Bell Court | Chuông lớn là vật cản thật; có thể đi vòng phía trên hoặc phía dưới. Đài lửa thứ hai nằm phía phải; đường vòng có một bình máu và lời khắc của người giữ tháp. |
| Phòng kéo chuông — Chain Chamber | Thắp đài cuối cùng đánh thức lính tinh nhuệ cầm thương. Đủ ba đài và hạ lính mới mở cầu đá. Điểm nghỉ cạnh cầu hồi đầy máu/bình và đặt checkpoint. |
| Đỉnh tháp — The Ashen Belfry | Đấu trường dưới chuông nứt, vệt sáng đỏ đứng yên trên trời, trận đấu với Bellbound. |

**J/phím giữa/5** ưu tiên tương tác khi đứng gần đài lửa, vật phẩm, lời khắc, điểm nghỉ hoặc cửa. Ở nơi khác, cùng phím này chém kiếm. Có thể quay lại các khu trước nếu bỏ sót một đài. Đài đã thắp giữ nguyên suốt hành trình; nhấn lại gần đài để đọc lời kể. Ba lời kể dần hé lộ những người giữ tháp đã tự xích mình vào chuông, chờ một bình minh không bao giờ tới.

Kẻ địch trong tối chỉ hiện thành bóng giáp. Trong bán kính **78 px** của đài đã thắp, chúng hiện rõ và tốc độ di chuyển giảm từ **25 xuống 13 px/giây**. Dụ địch vào ánh sáng để tạo khoảng phản công. Vùng đòn đánh luôn có viền vàng và gạch đỏ sáng, không chịu lớp tối của cảnh. Lời kể nằm ngoài vùng đi lại để không che cảnh báo. Lính thường có 48 HP; lính cầm thương có 96 HP.

Bellbound là người khổng lồ khom lưng trong giáp quấn xích, cao khoảng 70 px, kéo chùy lõi chuông. Boss có **600 HP**, các đòn chậm hơn Black Oathkeeper:

| Đòn | Cách né và phản công |
|---|---|
| Quét xích — Chain Sweep | Hình quạt trước mặt, bán kính 85 px, gây 24 sát thương. Có khoảng an toàn sau lưng. Cảnh báo 900 ms. |
| Nện chuông — Bell Crush | Khóa vị trí khi bắt đầu cảnh báo 1050 ms; vòng nện bán kính 26 px, gây 34 sát thương. Chùy mắc nền trong cửa sổ hồi 1250 ms. |
| Hồi chuông tang — Funeral Toll | Bốn vòng rỗng bán kính 30/60/90/120 px lan lần lượt, mỗi vòng có cảnh báo riêng 650/550/550/550 ms và gây 22 sát thương. Có thể chọn thời điểm lăn xuyên qua. |

Ở **300 HP**, boss hoàn tất đòn đang báo rồi chuyển giai đoạn. Chuông rơi trong 1100 ms và trở thành vật cản giữa sân; cả đi bộ và lăn đều phải vòng qua. Nhân vật đang nằm dưới chuông được đẩy nhẹ ra ngoài để không bị kẹt. Giai đoạn II giữ ba đòn quen thuộc, tiếp cận/nối đòn nhanh hơn, cửa sổ hồi ngắn hơn 200 ms và vòng nện rộng 30 px.

Khi thắng, tiếng chuông dừng; nhận **Mảnh Bình Minh thứ nhất** đúng một lần. Vệt đỏ bắt đầu chuyển động, hé lộ thứ đang thở trong Heart of Dawn. Sau ít nhất 3 giây có thể nhấn J để trở lại khu nghỉ. Đây là kết thúc chương hiện có; phần bên trong Heart of Dawn chưa được xây dựng.

Chết trong tháp dùng chuỗi ngã → tối → tỉnh dậy như trước. Trước khi hạ lính tinh nhuệ, hồi sinh bên tàn lửa ở bậc thềm; sau đó hồi sinh ở điểm nghỉ trước boss. Đài lửa, lính đã hạ, vật phẩm đã nhặt và mảnh đã nhận được giữ nguyên. Bellbound chưa bị hạ sẽ bắt đầu trận mới với đầy máu; Black Oathkeeper không xuất hiện lại. Tiến trình chỉ nằm trong phiên chơi, chưa lưu ra bộ nhớ điện thoại.

## Màn hình chính và giới thiệu cốt truyện

Màn hình chính có nền tàn tích, ánh lửa và sương mù chuyển động. Lên/xuống hoặc W/S chuyển lựa chọn; J/phím giữa xác nhận. Có bắt đầu hành trình, điều khiển, bật/tắt âm thanh và thoát.

Xác nhận BEGIN JOURNEY sẽ mở ba cảnh pixel có chuyển mờ qua đen, mỗi cảnh 4,5 giây (tổng khoảng 13,5 giây):

1. **The Fallen Kingdom**: mặt trời đã tắt, vương quốc hóa tro; một lời thề bị phá vỡ trói cả thế giới vào cái chết.
2. **The Black Oath**: Oathkeeper giữ ngọn lửa cuối cùng trong tầng hầm ẩm tối. Sau cổng của hắn là Heart of Dawn, hy vọng khôi phục ánh sáng.
3. **The Last Knight**: hiệp sĩ cuối cùng lên đường giành lại bình minh và cứu thế giới.

J/phím giữa bỏ qua toàn bộ giới thiệu và chuyển tới cảnh bước qua cửa đá; P tạm dừng; Q trở về menu. Giới thiệu tự kết thúc rồi vào trận boss đầu, chỉ diễn ra một lần trong mỗi hành trình. Trong giới thiệu, người chơi không bị đánh hoặc tiêu hao bình máu. Sau trận đấu, game tự chuyển tới lửa trại bất kể thắng thua; lối sang tháp đã mở. Chọn hành trình mới từ menu sẽ phát lại giới thiệu từ đầu.

Giao diện dùng tiếng Anh với font bitmap. Có thể chỉnh thời lượng giới thiệu trong `Balance.STORY_SCENE_TIME` và `STORY_SCENES`, thời lượng câu dẫn vào tháp trong `Balance.BELFRY_QUOTE_TIME`.

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
| Chém ngang | 430 / 330 ms | 45 | 420 / 330 ms |
| Bổ kiếm | 500 / 400 ms | 62 | 470 / 380 ms |
| Lao tới | 550 / 450 ms | 52 | 450 / 360 ms |
| Đập đất | 580 / 480 ms | 58 | 500 / 410 ms |

Chém ngang có bán kính 82 pixel; bổ kiếm dài 145 pixel, rộng 36 pixel; lao tối đa 175 pixel, rộng 40 pixel và dừng ở ranh đấu trường; đập đất có bán kính 96 pixel. Thời gian gây sát thương của bốn đòn lần lượt là 150/130/250/180 ms. Trước vùng cảnh báo, boss còn có 130 ms chuẩn bị.

Boss vào giai đoạn II khi dưới 360 HP, sau 850 ms chuyển trạng thái. Tốc độ tiếp cận tăng từ 40 lên 48 pixel/giây; nhịp tìm/chọn đòn giảm từ 300 xuống 210 ms và chuỗi đòn thay đổi. Bản 1.10.0 tăng độ khó bằng nhịp nhanh, vùng đánh rộng và sát thương cao, vẫn giữ 720 HP và cần 30 nhát chém trúng để thắng. Cảnh báo không bị bỏ qua. Không có sát thương do chỉ chạm thân boss.

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
python3 tools/package_release.py
```

Windows: mở `build.bat`; script tự tìm Python 3 đã cài và có thể dùng runtime đi kèm Codex nếu có. Không bắt buộc có lệnh `py`. Nếu Python đã có trong PATH, dùng `python build.py` và `python test.py`; cũng có thể gọi trực tiếp `python.exe`. Biến `ASHGATE_PYTHON` cho phép chọn đường dẫn Python cho `build.bat`.

`build.py` dùng Eclipse ECJ để tạo class version 45, biên dịch với CLDC/MIDP bootclasspath, sau đó ProGuard `-microedition` tạo StackMap dành cho CLDC. Kết quả nằm trong `dist/`; kích thước JAR trong JAD được cập nhật tự động. Manifest là ASCII không BOM.

Sau khi kiểm tra đạt, `tools/package_release.py` tạo ZIP điện thoại theo phiên bản trong manifest. Công cụ xác nhận phiên bản/kích thước JAD và từng byte JAR/JAD/hướng dẫn trong ZIP khớp các tệp đã build; ZIP chỉ gồm ba tệp này.

## Cấu trúc mã nguồn

- `src/Balance.java`: cấu hình máu, sát thương, tốc độ, phạm vi và thời gian.
- `src/World.java`: trạng thái trận đấu, hitbox, boss AI, hồi máu, né, thắng/thua, reset.
- `src/HeroSprites.java`: tải atlas và ánh xạ 144 khung sprite vào sáu trạng thái.
- `src/RestSprites.java`: atlas riêng 24 dáng ngồi nghỉ, phát ngược để đứng dậy và lặp nhịp thở khi ngồi.
- `src/BossSprites.java`: tải atlas 32 dáng boss, chọn hướng/trạng thái, giữ điểm chân và vẽ giật người/vòng sáng giai đoạn II.
- `src/KnightArt.java`: bộ giáp vẽ bằng mã và các lời gọi chuyển tới bộ khung boss.
- `src/SkeletonBossArt.java`: phương án boss bộ xương trong giáp vẽ bằng mã, cao 88 px, với điểm tay dùng chung với kiếm.
- `src/ArenaArt.java`: nền tầng hầm được dựng sẵn, vòm thấp/song sắt/tường ẩm/ống cũ và ánh đèn, giọt nước, gợn nước, sương chuyển động phía sau nhân vật.
- `src/BonfireArt.java`: khu nghỉ an toàn, ánh lửa trại, tàn tích, cổng cũ đã sập và lối sang tháp chuông.
- `src/Belfry.java`: năm khu, đài lửa, địch trong bóng tối, khám phá, vật cản, checkpoint và tiến trình chương.
- `src/BelfryArt.java`: bậc thềm ngoài trời, hành lang, sân chuông, phòng xích, đỉnh tháp và ánh sáng theo đài lửa.
- `src/EnemySprites.java`: hai atlas lính, bóng giáp trong tối, bốn hướng và các pha đi/đánh/ngã.
- `src/Bellbound.java` và `src/BellboundArt.java`: boss thứ hai, vùng cảnh báo dùng chung với sát thương, chuông rơi, di chuyển vòng vật cản và giáp quấn xích.
- `src/BellboundSprites.java`: atlas 40 dáng thân boss và điểm tay cho xích/chùy vẽ riêng.
- `src/VietnameseText.java`: tiện ích font bitmap có dấu dự phòng; câu dẫn hiện tại dùng tiếng Anh trong `Art.java`.
- `src/Art.java`: đồ họa pixel, hoạt ảnh theo trạng thái, font bitmap, cảnh báo và HUD.
- `src/Game.java`: nhận phím, tách nhấn mới khỏi giữ phím, vòng lặp, tạm dừng, âm thanh.
- `src/AshGate.java`: vòng đời MIDlet.
- `tests/`: kiểm tra mô hình chiến đấu, đầu vào Canvas và render thật bằng MicroEmulator.
- `preview/`: ảnh 320 × 240 được xuất từ renderer Java ME của bản JAR.
- `TEST-RESULTS.txt`: kết quả kiểm tra của bản bàn giao.

Mô phỏng dùng bước 33 ms (~30 lần/giây); thời gian thực tế được lượng tử theo bước này. Bản chậm sẽ không nhảy vượt toàn bộ một đòn để bù khung hình bị mất. Hình nền và lớp cảnh báo trong suốt được cache riêng; hiệu ứng không vẽ vào ảnh nền đã giữ. Không cần tải sprite bên ngoài. Hiệu ứng âm thanh dùng `Manager.playTone` và tự bỏ qua khi máy không hỗ trợ.

## Đã kiểm tra và giới hạn

- 74 kiểm tra chiến đấu tự động: hướng/range, một hit mỗi đòn, bốn đòn boss, cảnh báo không gây sát thương, né, cooldown, hồi máu và ngắt hồi, giới hạn đấu trường, chuyển giai đoạn, thắng/thua/reset; kiểm tra phạm vi và sát thương tăng, báo chiêu/hồi chiêu nhanh hơn và thời điểm kết thúc cửa sổ phản công.
- 7 kiểm tra đầu vào trên Canvas MicroEmulator: bắt đầu, giữ phím chém, nhấn lại, tạm dừng, đi chéo, về đầu và hướng dẫn.
- 17 kiểm tra menu/cốt truyện: chọn menu, âm thanh, tạm dừng, bỏ qua, tự chuyển ba cảnh, hủy/vào lại; J không bỏ qua phần ngất hoặc tự tái đấu sau thắng/thua.
- 134 kiểm tra lửa trại: hai nhánh thắng/thua, chờ thắng đúng 5 giây, ngã/mờ/đen/tỉnh dậy, thứ tự khung ngã và thức dậy, khóa đầu vào, phục hồi máu/bình, giữ kết quả, hoạt ảnh ngồi/đứng, nghỉ bên lửa, cổng cũ bị khóa, lối đi tiếp sau thua, tạm dừng và xóa phím giữ khi chuyển cảnh.
- 45 kiểm tra cuộc chạm trán mở đầu một lần qua Canvas thật: thắng/thua đều về trại rồi sang tháp, J/phím giữa/5 không tái đấu ở cổng cũ, quay lại trại/reset/cảnh cửa cũ không phát lại boss, chết trong chương sau, kết thúc Bellbound giữ đúng kết quả trận đầu và hành trình mới đặt lại tiến trình.
- 73 kiểm tra chương Tháp Chuông Tro Tàn: mở lối sau thắng hoặc thua, câu dẫn 1,8 giây chỉ lần ghé đầu và không lặp khi quay lại/hồi sinh, khoảng khám phá ban đầu, lính tuần tự, ánh sáng làm chậm, ba đài, hai đường vòng chuông, vật phẩm một lần, cầu/checkpoint, chết/tỉnh dậy, tương tác J và một kịch bản hoàn tất cả chương bằng cửa sổ chém thật.
- 75 kiểm tra Bellbound: ba đòn khóa hướng/vị trí, phạm vi báo khớp sát thương, bốn vòng tuần tự, né, hồi đòn, đổi giai đoạn, chuông vật lý, chống kẹt khi chuông rơi và đi vòng hai phía.
- 32 kiểm tra render chương mới, xuất 22 ảnh MIDP: câu dẫn nhỏ ở nửa dưới và không hiện lại khi quay về, cảnh báo sáng trong tối, địch hiện rõ trong lửa, lời kể không che vùng nguy hiểm, xóa cảnh báo cũ, chuông trên trần biến mất khi rơi, retry phục hồi chuông, kết thúc và tỉnh dậy. Xem `preview/belfry-entrance-quote.png`, `preview/belfry-lit-warning.png`, `preview/bellbound-phase-two.png`, `preview/belfry-dawn-shard.png`.
- Kiểm tra miễn sát thương cả đầu/cuối động tác lăn với bốn đòn boss, và nhận lại sát thương sau khi động tác kết thúc.
- 16 kiểm tra nhấn hai lần: một lần không né, đủ hướng, chéo, cửa sổ 280 ms, giữ/tự lặp không né, cooldown, khóa khi chém/uống, tạm dừng/ra nền và loại bỏ phím né cũ.
- Kiểm tra các dáng bốn hướng khác nhau, chu kỳ bước thay đổi và render các trạng thái chém, lăn, uống và chịu đòn ở cả bốn hướng.
- Kiểm tra 32 dáng boss tải từ atlas thật: thân đứng 88 px so với nhân vật 44 px, xương nhìn rõ ở bốn hướng, alpha nhị phân, không cắt kiếm và kiếm vẫn hiện trong mọi khung bước, đế giày ổn định khi bước/chém/ngã; giật người và giai đoạn II vẫn dùng atlas. Kiểm tra khởi tạo, tiếp cận và lao về mép bắc ở cả hai giai đoạn để boss không lấn HUD; bộ khung dự phòng cũng được render riêng.
- Kiểm tra 24 dáng ngồi nghỉ, 64 dáng lính và 40 dáng thân Bellbound từ atlas thật: bốn hướng, alpha nhị phân, điểm chân ổn định, vũ khí đầy đủ; bước theo thời gian di chuyển thực và đứng khi dừng, ánh sáng/tối theo đài lửa, trúng đòn và ngã. Chuỗi hoạt ảnh được xuất bằng MIDP ở 320 × 240; ngồi/đứng giữ vị trí và thở lặp khi nghỉ.
- Kiểm tra nền phòng không đè lên nhân vật/HUD, hiệu ứng không tích lũy khi vẽ lại và khung tạm dừng ổn định. Lớp cảnh báo trong suốt của cả bốn đòn khớp đường biên vùng sát thương và xóa đúng vùng cũ khi đổi đòn/vị trí.
- Xuất thành công 30 khung hình bằng implementation MIDP của MicroEmulator và kiểm tra bố cục trực quan; xác nhận khung tối hoàn toàn che cả nhân vật/HUD, màn lửa trại không vẽ boss hoặc vùng sát thương. Xem `preview/bonfire-after-defeat.png`, `preview/bonfire-after-victory.png` và `preview/camp-wake-mid.png`.
- Kiểm tra class version, StackMap, manifest không BOM và kích thước JAD khớp JAR.

**Chưa chạy trực tiếp trên Nokia E72 thật, chưa đo FPS/âm thanh/độ nhận phím trên thiết bị.** Việc chạy giao diện giả lập bằng tay trên máy tính của người dùng cũng chưa được xác nhận; các kiểm tra ở đây chạy bằng Java desktop không có cửa sổ. Nếu máy báo lỗi cài, gửi nguyên văn lỗi và xác nhận đã chép đúng cặp JAR/JAD cùng phiên bản.

Bản hiện có gồm giới thiệu, Black Oathkeeper, khu nghỉ và chương Tháp Chuông Tro Tàn với Bellbound; chưa có phần bên trong Heart of Dawn. Không có lưu trận; thoát ứng dụng hoặc chọn hành trình mới sẽ bắt đầu lại. Giao diện và câu dẫn trong game dùng tiếng Anh.

## Tài liệu kỹ thuật

- Oracle Java ME: https://docs.oracle.com/javame/mobile.html
- Oracle quy trình biên dịch và preverify: https://docs.oracle.com/javame/dev-tools/jme-sdk-3.0-mac/UserGuide-html/z400007747176.html
- Guardsquare cấu hình Java ME: https://www.guardsquare.com/manual/configuration/examples
- MicroEmulator: https://github.com/tisoft/microemu
- Nokia E72 Data Sheet (tài liệu Nokia, bản lưu): https://images-eu.ssl-images-amazon.com/images/G/03/electronics/Nokia_E72_Data_Sheet._V185028282_.pdf — xác nhận màn hình QVGA 320 × 240. Gói game nhắm CLDC 1.1 / MIDP 2.0; kết quả chạy trên thiết bị thật vẫn cần kiểm tra trực tiếp.
