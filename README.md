# live
基于websocket-flv协议的直播

前端:
目前只测试了 pc-谷歌浏览器 和 mobile-火狐浏览器.
html5 MediaRecorder 录制视频数据(webm)上传至服务, 用flv.js播放websocket-flv协议的直播流.

后端:
java实现,ffmepg转流(webm --> flv),手动解析flv数据流后推到前端显示

效果:
1. 现在直播延迟接近1s.



