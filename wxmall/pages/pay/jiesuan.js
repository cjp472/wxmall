var app = getApp();
Page({
  data: {
    detail: {},
    items:'',
    receiver:null,
    memo:''
  },
  
  onShow: function (){
      this.setData({  //从选择地址页面back，更新
        receiver: app.data.receiver
      })
  },
  onLoad: function (param) {
   app.data.receiver=null; 
   var that = this;
   var receiverId="";
   if (param.receiverId){ 
     receiverId = param.receiverId;
   }
   app.ajaxJson('/pay/balance', { items: param.items, receiverId: receiverId }, function (res) {
      var json = res.data;
      if (json.code != 200) {
        wx.showModal({
          showCancel: false,
          title: '错误',
          content: json.msg ? json.msg : "500",
        })
        return;
      }
      app.data.receiver = json.data.receiver;
      that.setData({
        detail: json.data,
        receiver: json.data.receiver,
        items: param.items
      });
   })
  },
  created: function (){
    var that = this;
    if (that.data.receiver == null){
        wx.showToast({
            title: '收货地址为空',
        })
        return;
    }
    
    wx.showLoading({
      mask: true,
      title: '正在提交订单数据，请稍侯...',
    });
    var items=that.data.items;

    var receiverId = that.data.receiver.id;
    var memo=that.data.memo;
    app.ajaxJson('/order/create', { items: items, receiverId: receiverId,memo:memo }, function (res) {
      wx.hideLoading();
      var json = res.data;
      if (json.code != 200) {
        wx.showModal({
          showCancel: false,
          title: '订单创建错误！',
          content: json.msg ? json.msg : "500",
        })
        return;
      }
     //pay....................
      that.wxPrepay(res.data.data)

    },function(){
      wx.hideLoading();
      wx.showModal({
        showCancel: false,
        title: '订单创建错误！',
        content: json.msg ? json.msg : "500",
      })
    })
  },
  bindTextAreaBlur: function (e) {
    //console.log(e.detail.value);
    this.setData({
      memo: e.detail.value
    })
  },
  addAddress: function(e){
    var that=this;
    wx.navigateTo({
      url: '../select-address/index'
    });
  },
  selectAddress:function(e){
    //address id 
    var id =e.currentTarget.id;  
    wx.navigateTo({
      url: '../select-address/index?id=' + id
    });
  },
  wxPrepay: function (orderId) {
    wx.showLoading({
      mask: true,
      title: '正在提交微信支付数据，请稍侯...',
    });
   var req={};
   req.orderId = orderId;
    app.ajaxJson('/pay/wxAppPrepareToPay',req,function(res){
        if (res.data.code == 200) {
          wx.hideLoading();
          let wxPay = res.data.data;
          wx.requestPayment({
            'timeStamp': wxPay.timeStamp,
            'nonceStr': wxPay.nonceStr,
            'package': wxPay.packageValue,
            'signType': wxPay.signType,
            'paySign': wxPay.paySign,
            'success': function (res) {
              wx.showModal({
                showCancel: false,
                title: '提示',
                content: '支付成功！',
                success: function (res) {
                  wx.navigateBack({
                    delta: 2
                  })
                }
              })
            },
            'fail': function (res) {
              var msg = '支付失败！请到‘个人->订单’再支付。';
              if (res.errMsg == 'requestPayment: fail cancel') {
                msg = '取消支付！请到‘个人->订单’再支付。'
              }
              wx.showModal({
                showCancel: false,
                title: '提示',
                content: msg,
                success: function (res) {
                  wx.navigateBack({
                    delta: 2
                  })
                }
              });
            }
          })

        } else {
          wx.hideLoading();
          wx.showModal({
            title: '提示',
            showCancel: false,
            content: '支付失败！请到‘个人->订单’再支付。',
            success: function (res) {
              wx.navigateBack({
                delta: 2
              })
            }
          });
          
        }

      },
      function() {
        wx.hideLoading();
        wx.showModal({
          title: '提示',
          showCancel: false,
          content: '支付失败！请到‘个人->订单’再支付。',
          success: function (res) {
            wx.navigateBack({
              delta: 2
            })
          }
        });
        
      }
    );
  }
})