//index.js
//获取应用实例
var app = getApp()
Page({
  data: {
    hotCount:0,
    indexCount:0,
    commondCount:0,
    newCount:0
  },
 
  onLoad: function () {
    var that = this;
    var that = this;
    app.ajaxJson('/index', {}, function (res) {
      var json = res.data;
      if (json.code != 200) {
        wx.showModal({
          showCancel: false,
          title: '服务器错误',
          content: json.msg ? json.msg : "500",
        })
        return;
      }
      var hotCount=0;
      if (json.data.hotProducts){
        hotCount = json.data.hotProducts.lentgh;
      }
      var newCount=0;
      if (json.data.newProducts){
        newCount = json.data.newProducts.length;
      }

      var indexCount = 0;
      if (json.data.newProducts) {
        indexCount = json.data.indexProducts.length;
      } 
      var commondCount=0;
      if (json.data.commondProducts){
        commondCount = json.data.commondProducts.length;
      }

      that.setData({
        shop: json.data.shop,
        hotProducts: json.data.hotProducts,
        hotCount: hotCount,
        newProducts: json.data.newProducts,
        newCount: newCount,
        indexProducts: json.data.indexProducts,
        indexCount: indexCount,
        commondProducts: json.data.commondProducts,
        commondCount: commondCount
      });
     
    });
  },
  toDetails:function(e){
    var param = e.currentTarget.dataset;
    var id = param["id"];
    wx.navigateTo({
      url: '../product/detail/detail?id=' + id
    })

  }
  
})
