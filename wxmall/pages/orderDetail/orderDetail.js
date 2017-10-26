var app = getApp();
Page({
  data: {
    order: null
  },
  onLoad: function (param) {
    if (!param.id) {
      return;
    }
    var that=this;
    var req={};
    req.orderId=param.id;
    app.ajaxJson('/order/detail', req, function (res) {      
      var json = res.data;
      if (json.code != 200) {
        return;
      }
    
      that.setData({
        order: json.data
      });
      
    });
  },
  cancelOrder:function(){
    var that=this;
    wx.showModal({
      title: '提示',
      content: '确定要取消订单位？',
      success: function (conf) {
        if (conf.confirm) {   
          var req={};
          req.orderId=that.data.order.orderId;
          app.ajaxJson('/order/cancel', req, function (res) {
            var json = res.data;
            if (json.code != 200) {
              wx.showModal({
                title: '提示',
                showCancel: false,
                content: '取消息失败！'
              });
              return;
            }
            wx.navigateTo({
              url: '../order/order'
            })

          });
        }
      } 
    })     
  },
  wxPrepay: function (orderId) {
    wx.showLoading({
      mask: true,
      title: '正在提交微信支付数据，请稍侯...',
    });
    var req = {};
    req.orderId = this.data.order.orderId;
    app.ajaxJson('/pay/wxAppPrepareToPay', req, function (res) {
      if (res.data.code == 200) {
        wx.hideLoading();
        let wxPay = res.data.data;
        wx.requestPayment({
          'timeStamp': wxPay.timeStamp,
          'nonceStr': wxPay.nonceStr,
          'package': wxPay.packageValue,
          'signType': wxPay.signType,
          'paySign': wxPay.paySign,
          'success': function (rx1) {
            wx.showModal({
              showCancel: false,
              title: '提示',
              content: '支付成功！',
              success: function (rx2) {
                wx.navigateTo({
                  url: '../order/order'
                })
              }
            })
          },
          'fail': function (rx) {
            var msg = '支付失败！';
            if (rxerrMsg == 'requestPayment: fail cancel') {
              msg = '取消支付！'
            }
            wx.showModal({
              showCancel: false,
              title: '提示',
              content: msg
            });
          }
        })

      } else {
        wx.hideLoading();
        wx.showModal({
          title: '提示',
          showCancel: false,
          content: '支付失败！'
        });

      }

    },
      function () {
        wx.hideLoading();
        wx.showModal({
          title: '提示',
          showCancel: false,
          content: '支付失败！'
        });

      }
    );
  }
})  