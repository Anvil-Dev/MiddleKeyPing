# 标点配置与地图联动

标点每帧绘制一次，并使用世界渲染的最终投影，随行走晃动、受伤摇晃和动态视野角同步定位。隐藏游戏界面时隐藏标点，退出世界时清理投影状态。

客户端配置由 AnvilLib 保存，可通过模组配置界面修改。`maxPingDistance` 控制世界内拾取距离，范围为 16 至 1024 格；`allowPingEmpty` 控制是否允许标记未命中的位置；`maxPingCount` 控制同时保留的最大标点数，范围为 1 至 64，默认为 6。四类标点的图标和距离文字分别使用 `genericColor`、`warningColor`、`gotoColor`、`enemyColor` 及对应的 `TextColor` 配置，颜色格式为 `#RRGGBBAA`。

同时安装 Aleeve Atlas 0.0.1 系列构建版或更高版本时，MiddleKeyPing 会自动接管世界内和 Atlas 世界地图的中键操作、轮盘内容、世界内渲染与网络发送。在地图上短按中键发送“一般”标点；长按打开与世界内相同的轮盘，选项依次为“一般”、“警告”、“前往”、“敌人”，目标位置采用地图光标所指的世界坐标与地图缓存高度。松开中键即可发送选中项并返回地图，按 Esc 或点击鼠标可取消轮盘。

MiddleKeyPing 使用内嵌的 AnvilLib Integration 模块加载 Atlas 客户端联动。

世界内的标点按键使用 MiddleKeyPing 的按键绑定。Atlas 世界地图使用鼠标中键，制图选区中的中键继续用于拖动地图。Atlas 原有的世界内标点按键与临时标点覆层会自动关闭。

多人游戏的标点收发需要服务端及参与的客户端均安装 MiddleKeyPing。坐标标点会广播给同维度的玩家，实体标点则会广播给正在追踪该实体的玩家；客户端接收后将同步显示在 Atlas 小地图与世界地图上。Atlas 的 `ping.channelMode` 配置不影响此类标点的发送。通过 API 注入或接收到的 Atlas 地图标点，同样使用 MiddleKeyPing 进行世界内渲染。

标点默认保留 300 个游戏刻（约 15 秒）。实体标点会跟随已加载的目标；若目标尚未加载则等待其出现，目标卸载后将保留其最后坐标直至过期。标点数量超过 `maxPingCount` 时会移除最早的标点，并同步移除对应的地图标点；退出世界或切换维度时会清空所有标点。

执行 `./gradlew.bat build --console=plain` 时，从 `https://server.cjsah.net:1002/maven/` 解析 `dev.dubhe.map:aleeve_atlas-neoforge-1.21.11:0.0.1+build.+`。Atlas 仅作为可选编译依赖，不会内嵌到 MiddleKeyPing 的构建产物中。
