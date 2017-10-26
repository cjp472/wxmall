
var app = getApp()
Page({
  data: {
      userInfo: {},
    payedCount:0,
    shippedCount:0,
    unpayedCount:0,
    complateCount:0,
    userInfo: {}
  },
  navigateToAddress: function () {
    wx.navigateTo({
      url: '../select-address/index?fromPage=user'
    });
  },
 
  
  onLoad: function () {
    var that = this
    app.ajaxJson('/user/index', {}, function (res) {
      var json = res.data;
      if (json.code == 200) {
        that.setData({
          userInfo: app.data.userInfo,
          payedCount: json.data.payedCount,
          shippedCount: json.data.shippedCount,
          unpayedCount: json.data.unpayedCount,
          complateCount: json.data.complateCount
        });
      }
    });    
  }
})
