//获取应用实例
var app = getApp()
var initProvince= '请选择省';
var  initCity= '请选择市';
var initDistrict='请选择区';

Page({
  data: {
    provinces: [],
    citys: [],
    districts: [],
    provincesId: [],
    citysId: [],
    districtsId: [],
    selProvince: initProvince,
    selCity: initCity,
    selDistrict: initDistrict,
    selProvinceIndex: 0,
    selCityIndex: 0,
    selDistrictIndex: 0,
    areaId:'',
    id:null,
    is_default:0
  },
  onLoad: function (e) {
    var that=this;
    this.initCityData(1);
    var id = e.id;
    if (id) {
      // 初始化原数据
      wx.showLoading();
      app.ajaxJson('/receiver/edit', {
        id: id
      }, function (res) {
        var json = res.data;
        if (json.code != 200) {
          wx.showModal({
            title: '提示',
            content: '无法获取快递地址数据',
            showCancel: false
          })
          return;
        }
        wx.hideLoading();
        that.setData({
          id: id,
          addressData: res.data.data,
          selProvince: res.data.data.province,
          selCity: res.data.data.city,
          selDistrict: res.data.data.district,
          areaId: res.data.data.area_id,
          is_default: res.data.data.is_default
        });
      });
    }
  },

  bindCancel: function () {
    wx.navigateBack({})
  },
  
  bindSave: function(e) {
    var that = this;
    var linkMan = e.detail.value.linkMan;
    var address = e.detail.value.address;
    var mobile = e.detail.value.mobile;
    var areaPath = this.data.areaId;
    var id = this.data.id;
    var province = this.data.selProvince;
    var city = this.data.selCity;
    var is_default = this.data.is_default;
    var district = this.data.selDistrict;
    if (linkMan == "") {
      wx.showModal({
        title: '提示',
        content: '请填写联系人',
        showCancel: false
      })
      return
    }
    if (mobile == "") {
      wx.showModal({
        title: '提示',
        content: '请填写手机号码',
        showCancel: false
      })
      return
    }
    if (this.data.selProvince == initProvince) {
      wx.showModal({
        title: '提示',
        content: '请选择省',
        showCancel: false
      })
      return
    }
    if (this.data.selCity == initCity) {
      wx.showModal({
        title: '提示',
        content: '请选择市',
        showCancel: false
      })
      return
    }
    if (this.data.selDistrict == initDistrict){
      wx.showModal({
        title: '提示',
        content: '请选择区',
        showCancel: false
      })
      return
    }

    if (address == ""){
      wx.showModal({
        title: '提示',
        content: '请填写详细地址',
        showCancel:false
      })
      return
    }
    app.ajaxJson('/receiver/save', {
      receiverId:id,
      address: address,
      name: linkMan,
      phone : mobile,
      area_id : areaPath,
      province: province,
      city: city,
      district: district,
      is_default: is_default
    }, function (res) {
      var json = res.data;
      if (json.code != 200) {
        return;
      }
     wx.navigateBack();
    });
  },
  radioChange:function(event){
    console.log(event.detail.value);
      this.setData({
        is_default: event.detail.value
      });
  },
  bindPickerProvinceChange: function (event) {
    var selIterm = this.data.provinces[event.detail.value];
    var selItermId = this.data.provincesId[event.detail.value];
    console.log(event.detail.value);
    this.setData({
      selProvince: selIterm,
      selProvinceIndex: event.detail.value,
      selCity: initCity,
      selDistrict: initDistrict,
      areaId: selItermId
    })
    this.initCityData(2, selItermId)
  },
   bindPickerCityChange: function (event) {
     var selIterm = this.data.citys[event.detail.value];
     var selItermId = this.data.citysId[event.detail.value];
    this.setData({
      selCity: selIterm,
      selCityIndex: event.detail.value,
      selDistrict: initDistrict,
      areaId: selItermId
    })
    this.initCityData(3, selItermId)
  },
   bindPickerChange: function (event) {
     var selIterm = this.data.districts[event.detail.value];
     var selItermId = this.data.districtsId[event.detail.value];
     if (selIterm  && event.detail.value) {
       this.setData({
         selDistrict: selIterm,
         selDistrictIndex: event.detail.value,
         areaId: selItermId
       })
     }
   },
  initCityData: function (level, obj) {
    var that=this;
    if (level == 1) {
      var pinkArray = [];
      var pinkIdArray = [];
      app.ajaxJson('/receiver/area', {}, function (res) {
        var json = res.data;
        if (json.code != 200) {
          return;
        }
        var jsonData = json.data;
         for (var i = 0; i < jsonData.length;i++){
           pinkArray.push(jsonData[i].name);
           pinkIdArray.push(jsonData[i].id);
        }
         that.setData({
           provinces: pinkArray,
           provincesId: pinkIdArray
         });
       
      });
    } else if (level == 2) {
      var pinkArray = [];
      var pinkIdArray = [];
      app.ajaxJson('/receiver/area', { parentId:obj}, function (res) {
        var json = res.data;
        if (json.code != 200) {
          return;
        }
        var jsonData = json.data;
        for (var i = 0; i < jsonData.length; i++) {
          pinkArray.push(jsonData[i].name);
          pinkIdArray.push(jsonData[i].id);
        }
        that.setData({
          citys: pinkArray,
          citysId: pinkIdArray
        });

      });
    } else if (level == 3) {
      var pinkArray = [];
      var pinkIdArray = [];
      app.ajaxJson('/receiver/area', { parentId: obj }, function (res) {
        var json = res.data;
        if (json.code != 200) {
          return;
        }
        var jsonData = json.data;
        for (var i = 0; i < jsonData.length; i++) {
          pinkArray.push(jsonData[i].name);
          pinkIdArray.push(jsonData[i].id);
        }
        that.setData({
          districts: pinkArray,
          districtsId: pinkIdArray
        });

      });
    }    
  }
})