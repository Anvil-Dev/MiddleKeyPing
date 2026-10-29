# 标点配置与地图联动

标点每帧绘制一次，并使用世界渲染的最终投影，随行走晃动、受伤摇晃和动态视野角同步定位。隐藏游戏界面时隐藏标点，退出世界时清理投影状态。

客户端配置由 AnvilLib 保存，可通过模组配置界面修改。`maxPingDistance` 控制世界内拾取距离，范围为 16 至 1024 格；`allowPingEmpty` 控制是否允许标记未命中的位置；`maxPingCount` 控制同时保留的标点数，范围为 1 至 64，默认 6。四类标点的图标和距离文字分别使用 `genericColor`、`warningColor`、`gotoColor`、`enemyColor` 及对应的 `TextColor` 配置，颜色格式为 `#RRGGBBAA`。

同时安装 Aleeve Atlas 0.0.2 或更新版本时，MiddleKeyPing 自动负责世界内和 Atlas 世界地图的中键操作、轮盘内容、世界内渲染与网络发送。地图短按发送一般标点；长按打开与世界内相同的轮盘，选项依次为一般、警告、前往、敌人，目标使用地图鼠标所指的世界坐标与地图缓存高度。松开中键发送选中项并返回地图，按 Esc 或点击鼠标取消轮盘。

MiddleKeyPing 使用内嵌的 AnvilLib Integration 模块加载 Atlas 客户端联动。

世界内的标点按键使用 MiddleKeyPing 的绑定。Atlas 世界地图使用鼠标中键，制图选区中的中键继续用于拖动地图。Atlas 原有的世界内按键与临时标点覆层会自动关闭。

多人标点发送和接收需要服务端及参与客户端安装 MiddleKeyPing。坐标标点广播给同维度玩家，实体标点广播给跟踪该实体的玩家；接收后同步显示在 Atlas 小地图与世界地图。Atlas 的 `ping.channelMode` 不参与这些标点的发送。地图内由 API 注入或收到的 Atlas 标点也使用 MiddleKeyPing 的世界内渲染。

标点默认保留 300 个游戏刻，约 15 秒。实体标点跟随已加载目标；目标尚未加载时等待其出现，卸载后保留最后坐标直到过期。超过 `maxPingCount` 时移除最早标点，并同步移除地图标点；退出世界或切换维度时清理标点。

编译前需先在相邻 `aleeve-atlas` 仓库执行 `./gradlew.bat publishToMavenLocal --console=plain`，再在本仓库执行 `./gradlew.bat build --console=plain`。Atlas 作为可选编译依赖，不会内嵌到 MiddleKeyPing 的构建产物中。
