//app.js
var md5 = require('utils/md5.js')
App({
  data: {
    userInfo: null,
    appId: 'wx23fa20cdbb6dd2b6'
  },

  onLaunch: function () {
    //调用API从本地缓存中获取数据
    // var logs = wx.getStorageSync('logs') || []
    // logs.unshift(Date.now())
    // wx.setStorageSync('logs', logs)
    
   // let extConfig = wx.getExtConfigSync ? wx.getExtConfigSync() : {}
   // var appId = extConfig["appid"];
   // this.data.appId = appId;
    
    //调用登录接口
    var me = this;
    wx.login({
        sucess: function (res) {},
        fail: function(res){
            me.toString(res);
        },
        complete: function(res){
            if(!res.code) return;
            me.ajaxJson("/user/login", { code: res.code }, function (res) {
                var json = res.data;
                if (json.code != 200){
                    wx.showModal({ showCancel: false, title: '登陆失败', content: json.msg ? json.msg : ""});
                }else{
                    console.log("=====sessionid:" + json.data.sessionId + ",openid:" + json.data.openid)
                    wx.setStorageSync('sessionid', json.data.sessionId);
                    wx.getUserInfo({
                        //withCredentials:true,
                        success: function (res) {
                            var userInfo = res.userInfo;
                            //对用户数据进行签名校验
                            var req={};
                            req.rawData= res.rawData;
                            req.signature=res.signature;
                            req.encryptedData=res.encryptedData;
                            req.iv=res.iv;
                            me.ajaxJson("/user/check", req, function(res){
                                var userRes = res.data;
                                if (userRes.code != 200){
                                    wx.showModal({ showCancel: false, title: '校验用户数据异常', content: userRes.msg ? userRes.msg : "" });
                                }else{
                                    me.toString(res);
                                    me.data.userInfo = userInfo;
                                    me.toString(userInfo)
                                }
                            });
                        },
                        fail: function (res) {
                            console.log("=================getUserInfo.fail");
                            me.toString(res);
                        }
                    })
                }
            })
        }
    })
  },
  getUserInfo: function (cb) {
    var that = this
    if (this.globalData.userInfo) {
      typeof cb == "function" && cb(this.globalData.userInfo)
    } else {
      //调用登录接口
      wx.login({
        success: function () {
          wx.getUserInfo({
            success: function (res) {
              that.globalData.userInfo = res.userInfo
              typeof cb == "function" && cb(that.globalData.userInfo)
            }
          })
        }
      })
    }
  },
  getDomain: function () {
    return "https://api.ybwqy.com/api"; //"http://127.0.0.1/api";
  },
  ajaxJson: function (url, paraMap, callback, failCallback){
    var me = this;
    var tokenMap = {};
    var timestamp = new Date().getTime();
    tokenMap.serverKey="2016";
    tokenMap.timestamp=timestamp;
    for(var key in paraMap){
      tokenMap[key]=paraMap[key];
    }
    var token = this.getToken(tokenMap, "bcttcwls789");
    paraMap.sign=token;
    paraMap.serverKey="2016";
    paraMap.timestamp=timestamp;
    // var paraMapStr = paraMap.toString();
    // var message = des.des("DBUMAMAAPI", paraMapStr, 1, 0);
    // message = des.stringToHex(message);
    wx.request({
      url: this.getDomain() + url,
      method: "POST",
      header: {
          'content-type': 'application/json',
          "appid": me.data.appId,
          "sessionid": wx.getStorageSync('sessionid') || "",
          "Cookie": "JSESSIONID=" + wx.getStorageSync('sessionid') || ""
      },
      data: paraMap,
      success: function (res) {
        console.log("invoke " +url+ " Interface===========")
        if (callback) callback(res);
      },
      fail: function (res) { 
        if (failCallback) failCallback();
      },
      complete: function (res) { }
    })
  },
  onShow: function () {
      console.log('===========App Show');
  },
  onHide: function () {
      console.log('===========App Hide');
  },
  
  objKeySort: function (obj) {//obj对象key排序的函数
    var newkey = Object.keys(obj).sort();
    var newObj = {};
    for(var i = 0; i<newkey.length; i++) {
      newObj[newkey[i]] = obj[newkey[i]];
    }
    return newObj;
  }, 
  getToken: function (obj, serverSecret){//生成请求Token
    var token = "";
    var hasAppKey = false;
    var tokenParam = this.objKeySort(obj);
    for(var key in tokenParam){
      if(key != "sign"){
        token += key+tokenParam[key];
      }
      if (key == "serverKey") hasAppKey = true;
    }
    if (!hasAppKey) throw "serverKey not found";
    token += "serverSecret" + serverSecret;
    //return byte2hex and md5
    console.log("============token:" + token)
    //utf-8
    token = this.Utf8Encode(token);
    return md5.hex_md5(token).toUpperCase();
  },
  toString: function(obj){
      for(var key in obj){
          console.log("key:" + key + ",value:" + obj[key]);
      }
  },
  Utf8Encode: function (str) {
    var utftext = "";
    for  (var  n  = 0;  n  <  str.length;  n++)  {
      var c = str.charCodeAt(n); if (c < 128) {
        utftext += String.fromCharCode(c);
      } else if((c > 127) && (c < 2048)){
        utftext += String.fromCharCode((c >> 6) | 192);
         utftext += String.fromCharCode((c & 63) | 128);
      }else{
        utftext+=String.fromCharCode((c >> 12) | 224);
        utftext+=String.fromCharCode(((c >> 6) & 63) | 128);
        utftext+=String.fromCharCode((c & 63) | 128);
      }
    }
   return utftext;  
  }  
})