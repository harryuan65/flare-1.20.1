# 信號槍 (Flaregun )

參考 D:\MinecraftModding\forge-1.20.1-47.4.16-mdk 的火焰發射器設計
使用 GeckoLib 設定3d model 與渲染 animation

左鍵發射(animation: fire) R鍵換彈(animation: reload)
我已經動畫、模型、texture、音效檔案放到 resources/assets/flaregun/animations 與 resources/assets/flaregun/geo, resources/assets/flaregun/textures, resources/assets/flaregun/sounds 中
發射時撥放 flaregun/fire.ogg ，裝填時撥放 flaregun/reload.ogg 音效

發射出去的子彈可以用簡單的 方塊代替就好 顏色 #E09D38 (如果辦得到)
飛行軌跡上會產生 煙 particle
受重力影響，可以先隨便設定一個你覺得適合的值
飛行時間2s 時間到了會停滯在最後位置直到消失
停滯後開始計算維持時間 20s
亮度15 自帶 flash particle 效果，會一直產生該 flash particle 直到消失

需要簡單的翻譯，en.json zh_tw.json 各一份。
