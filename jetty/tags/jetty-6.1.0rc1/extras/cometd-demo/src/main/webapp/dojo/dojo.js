/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(typeof dojo=="undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 6425 $".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.evalProp=function(_3,_4,_5){
if((!_4)||(!_3)){
return undefined;
}
if(!dj_undef(_3,_4)){
return _4[_3];
}
return (_5?(_4[_3]={}):undefined);
};
dojo.parseObjPath=function(_6,_7,_8){
var _9=(_7||dojo.global());
var _a=_6.split(".");
var _b=_a.pop();
for(var i=0,l=_a.length;i<l&&_9;i++){
_9=dojo.evalProp(_a[i],_9,_8);
}
return {obj:_9,prop:_b};
};
dojo.evalObjPath=function(_e,_f){
if(typeof _e!="string"){
return dojo.global();
}
if(_e.indexOf(".")==-1){
return dojo.evalProp(_e,dojo.global(),_f);
}
var ref=dojo.parseObjPath(_e,dojo.global(),_f);
if(ref){
return dojo.evalProp(ref.prop,ref.obj,_f);
}
return null;
};
dojo.errorToString=function(_11){
if(!dj_undef("message",_11)){
return _11.message;
}else{
if(!dj_undef("description",_11)){
return _11.description;
}else{
return _11;
}
}
};
dojo.raise=function(_12,_13){
if(_13){
_12=_12+": "+dojo.errorToString(_13);
}else{
_12=dojo.errorToString(_12);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_12);
}
}
catch(e){
}
throw _13||Error(_12);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_15){
return dj_global.eval?dj_global.eval(_15):eval(_15);
}
dojo.unimplemented=function(_16,_17){
var _18="'"+_16+"' not implemented";
if(_17!=null){
_18+=" "+_17;
}
dojo.raise(_18);
};
dojo.deprecated=function(_19,_1a,_1b){
var _1c="DEPRECATED: "+_19;
if(_1a){
_1c+=" "+_1a;
}
if(_1b){
_1c+=" -- will be removed in version: "+_1b;
}
dojo.debug(_1c);
};
dojo.render=(function(){
function vscaffold(_1d,_1e){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1d};
for(var i=0;i<_1e.length;i++){
tmp[_1e[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _21={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_21;
}else{
for(var _22 in _21){
if(typeof djConfig[_22]=="undefined"){
djConfig[_22]=_21[_22];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
var _25=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _26={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_27,_28){
this.modulePrefixes_[_27]={name:_27,value:_28};
},moduleHasPrefix:function(_29){
var mp=this.modulePrefixes_;
return Boolean(mp[_29]&&mp[_29].value);
},getModulePrefix:function(_2b){
if(this.moduleHasPrefix(_2b)){
return this.modulePrefixes_[_2b].value;
}
return _2b;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _26){
dojo.hostenv[_2c]=_26[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if(_2d.charAt(0)=="/"||_2d.match(/^\w+:/)){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2e?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _33=this.getText(uri,null,true);
if(!_33){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_36,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_3d){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3d]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_40){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_40]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_42){
var _43=_42.split(".");
for(var i=_43.length;i>0;i--){
var _45=_43.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_45)){
_43[0]="../"+_43[0];
}else{
var _46=this.getModulePrefix(_45);
if(_46!=_45){
_43.splice(0,i,_46);
break;
}
}
}
return _43;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_47,_48,_49){
if(!_47){
return;
}
_49=this._global_omit_module_check||_49;
var _4a=this.findModule(_47,false);
if(_4a){
return _4a;
}
if(dj_undef(_47,this.loading_modules_)){
this.addedToLoadingCount.push(_47);
}
this.loading_modules_[_47]=1;
var _4b=_47.replace(/\./g,"/")+".js";
var _4c=_47.split(".");
var _4d=this.getModuleSymbols(_47);
var _4e=((_4d[0].charAt(0)!="/")&&!_4d[0].match(/^\w+:/));
var _4f=_4d[_4d.length-1];
var ok;
if(_4f=="*"){
_47=_4c.slice(0,-1).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,!_49?_47:null);
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var _51=!_49?_47:null;
ok=this.loadPath(_4b,_51);
if(!ok&&!_48){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
}
}
if(!ok&&!_49){
dojo.raise("Could not load '"+_47+"'; last tried '"+_4b+"'");
}
}
if(!_49&&!this["isXDomain"]){
_4a=this.findModule(_47,false);
if(!_4a){
dojo.raise("symbol '"+_47+"' is not defined after loading '"+_4b+"'");
}
}
return _4a;
};
dojo.hostenv.startPackage=function(_52){
var _53=String(_52);
var _54=_53;
var _55=_52.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
_54=_55.join(".");
}
var _56=dojo.evalObjPath(_54,true);
this.loaded_modules_[_53]=_56;
this.loaded_modules_[_54]=_56;
return _56;
};
dojo.hostenv.findModule=function(_57,_58){
var lmn=String(_57);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_58){
dojo.raise("no loaded module named '"+_57+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_5a){
var _5b=_5a["common"]||[];
var _5c=_5a[dojo.hostenv.name_]?_5b.concat(_5a[dojo.hostenv.name_]||[]):_5b.concat(_5a["default"]||[]);
for(var x=0;x<_5c.length;x++){
var _5e=_5c[x];
if(_5e.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5e);
}else{
dojo.hostenv.loadModule(_5e);
}
}
};
dojo.require=function(_5f){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_60,_61){
var _62=arguments[0];
if((_62===true)||(_62=="common")||(_62&&dojo.render[_62].capable)){
var _63=[];
for(var i=1;i<arguments.length;i++){
_63.push(arguments[i]);
}
dojo.require.apply(dojo,_63);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_65){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_66,_67){
return dojo.hostenv.setModulePrefix(_66,_67);
};
dojo.setModulePrefix=function(_68,_69){
dojo.deprecated("dojo.setModulePrefix(\""+_68+"\", \""+_69+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_68,_69);
};
dojo.exists=function(obj,_6b){
var p=_6b.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_6e){
var _6f=_6e?_6e.toLowerCase():dojo.locale;
if(_6f=="root"){
_6f="ROOT";
}
return _6f;
};
dojo.hostenv.searchLocalePath=function(_70,_71,_72){
_70=dojo.hostenv.normalizeLocale(_70);
var _73=_70.split("-");
var _74=[];
for(var i=_73.length;i>0;i--){
_74.push(_73.slice(0,i).join("-"));
}
_74.push(false);
if(_71){
_74.reverse();
}
for(var j=_74.length-1;j>=0;j--){
var loc=_74[j]||"ROOT";
var _78=_72(loc);
if(_78){
break;
}
}
};
dojo.hostenv.localesGenerated;
dojo.hostenv.registerNlsPrefix=function(){
dojo.registerModulePath("nls","nls");
};
dojo.hostenv.preloadLocalizations=function(){
if(dojo.hostenv.localesGenerated){
dojo.hostenv.registerNlsPrefix();
function preload(_79){
_79=dojo.hostenv.normalizeLocale(_79);
dojo.hostenv.searchLocalePath(_79,true,function(loc){
for(var i=0;i<dojo.hostenv.localesGenerated.length;i++){
if(dojo.hostenv.localesGenerated[i]==loc){
dojo["require"]("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _7c=djConfig.extraLocale||[];
for(var i=0;i<_7c.length;i++){
preload(_7c[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7e,_7f,_80,_81){
dojo.hostenv.preloadLocalizations();
var _82=dojo.hostenv.normalizeLocale(_80);
var _83=[_7e,"nls",_7f].join(".");
var _84="";
if(_81){
var _85=_81.split(",");
for(var i=0;i<_85.length;i++){
if(_82.indexOf(_85[i])==0){
if(_85[i].length>_84.length){
_84=_85[i];
}
}
}
if(!_84){
_84="ROOT";
}
}
var _87=_81?_84:_82;
var _88=dojo.hostenv.findModule(_83);
var _89=null;
if(_88){
if(djConfig.localizationComplete&&_88._built){
return;
}
var _8a=_87.replace("-","_");
var _8b=_83+"."+_8a;
_89=dojo.hostenv.findModule(_8b);
}
if(!_89){
_88=dojo.hostenv.startPackage(_83);
var _8c=dojo.hostenv.getModuleSymbols(_7e);
var _8d=_8c.concat("nls").join("/");
var _8e;
dojo.hostenv.searchLocalePath(_87,_81,function(loc){
var _90=loc.replace("-","_");
var _91=_83+"."+_90;
var _92=false;
if(!dojo.hostenv.findModule(_91)){
dojo.hostenv.startPackage(_91);
var _93=[_8d];
if(loc!="ROOT"){
_93.push(loc);
}
_93.push(_7f);
var _94=_93.join("/")+".js";
_92=dojo.hostenv.loadPath(_94,null,function(_95){
var _96=function(){
};
_96.prototype=_8e;
_88[_90]=new _96();
for(var j in _95){
_88[_90][j]=_95[j];
}
});
}else{
_92=true;
}
if(_92&&_88[_90]){
_8e=_88[_90];
}else{
_88[_90]=_8e;
}
if(_81){
return true;
}
});
}
if(_81&&_82!=_84){
_88[_82.replace("-","_")]=_88[_84.replace("-","_")];
}
};
(function(){
var _98=djConfig.extraLocale;
if(_98){
if(!_98 instanceof Array){
_98=[_98];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_9c,_9d){
req(m,b,_9c,_9d);
if(_9c){
return;
}
for(var i=0;i<_98.length;i++){
req(m,b,_98[i],_9d);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _9f=document.location.toString();
var _a0=_9f.split("?",2);
if(_a0.length>1){
var _a1=_a0[1];
var _a2=_a1.split("&");
for(var x in _a2){
var sp=_a2[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _a6=document.getElementsByTagName("script");
var _a7=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_a6.length;i++){
var src=_a6[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_a7);
if(m){
var _ab=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_ab+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_ab;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_ab;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _b3=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_b3>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_b3+6,_b3+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _b5=window["document"];
var tdi=_b5["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}else{
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b9=null;
var _ba=null;
try{
_b9=new XMLHttpRequest();
}
catch(e){
}
if(!_b9){
for(var i=0;i<3;++i){
var _bc=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b9=new ActiveXObject(_bc);
}
catch(e){
_ba=e;
}
if(_b9){
dojo.hostenv._XMLHTTP_PROGIDS=[_bc];
break;
}
}
}
if(!_b9){
return dojo.raise("XMLHTTP not available",_ba);
}
return _b9;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_be,_bf){
if(!_be){
this._blockAsync=true;
}
var _c0=this.getXmlhttpObject();
function isDocumentOk(_c1){
var _c2=_c1["status"];
return Boolean((!_c2)||((200<=_c2)&&(300>_c2))||(_c2==304));
}
if(_be){
var _c3=this,_c4=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_c0.onreadystatechange=function(){
if(_c4){
gbl.clearTimeout(_c4);
_c4=null;
}
if(_c3._blockAsync||(xhr&&xhr._blockAsync)){
_c4=gbl.setTimeout(function(){
_c0.onreadystatechange.apply(this);
},10);
}else{
if(4==_c0.readyState){
if(isDocumentOk(_c0)){
_be(_c0.responseText);
}
}
}
};
}
_c0.open("GET",uri,_be?true:false);
try{
_c0.send(null);
if(_be){
return null;
}
if(!isDocumentOk(_c0)){
var err=Error("Unable to load "+uri+" status:"+_c0.status);
err.status=_c0.status;
err.responseText=_c0.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_bf)&&(!_be)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _c0.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_c8){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_c8);
}else{
try{
var _c9=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c9){
_c9=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_c8));
_c9.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_c8+"</div>");
}
catch(e2){
window.status=_c8;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_cb,_cc,fp){
var _ce=_cb["on"+_cc]||function(){
};
_cb["on"+_cc]=function(){
fp.apply(_cb,arguments);
_ce.apply(_cb,arguments);
};
return true;
}
function dj_load_init(e){
var _d0=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_d0!="domcontentloaded"&&_d0!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _d1=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_d1();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_d1);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&(djConfig["enableMozDomContentLoaded"]===true))){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.write("<scr"+"ipt defer src=\"//:\" "+"onreadystatechange=\"if(this.readyState=='complete'){dj_load_init();}\">"+"</scr"+"ipt>");
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _d2=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_d2=_d2.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_d2=_d2.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_d2.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _d3=new dojo.xml.Parse();
if(_d2.length>0){
for(var x=0;x<_d2.length;x++){
var _d5=document.getElementById(_d2[x]);
if(!_d5){
continue;
}
var _d6=_d3.parseElement(_d5,null,true);
dojo.widget.getParser().createComponents(_d6);
}
}else{
if(djConfig.parseWidgets){
var _d6=_d3.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_d6);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_db,_dc){
dj_currentContext=_db;
dj_currentDocument=_dc;
};
dojo._fireCallback=function(_dd,_de,_df){
if((_de)&&((typeof _dd=="string")||(_dd instanceof String))){
_dd=_de[_dd];
}
return (_de?_dd.apply(_de,_df||[]):_dd());
};
dojo.withGlobal=function(_e0,_e1,_e2,_e3){
var _e4;
var _e5=dj_currentContext;
var _e6=dj_currentDocument;
try{
dojo.setContext(_e0,_e0.document);
_e4=dojo._fireCallback(_e1,_e2,_e3);
}
finally{
dojo.setContext(_e5,_e6);
}
return _e4;
};
dojo.withDoc=function(_e7,_e8,_e9,_ea){
var _eb;
var _ec=dj_currentDocument;
try{
dj_currentDocument=_e7;
_eb=dojo._fireCallback(_e8,_e9,_ea);
}
finally{
dj_currentDocument=_ec;
}
return _eb;
};
}
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _ed=false;
var _ee=false;
var _ef=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_ed=true;
}else{
if(typeof this["load"]=="function"){
_ee=true;
}else{
if(window.widget){
_ef=true;
}
}
}
var _f0=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_f0.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_ed)&&(!_ef)){
_f0.push("browser_debug.js");
}
var _f1=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_f1=djConfig["baseLoaderUri"];
}
for(var x=0;x<_f0.length;x++){
var _f3=_f1+"src/"+_f0[x];
if(_ed||_ee){
load(_f3);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_f3+"'></scr"+"ipt>");
}
catch(e){
var _f4=document.createElement("script");
_f4.src=_f3;
document.getElementsByTagName("head")[0].appendChild(_f4);
}
}
}
})();
dojo.debug=function(){
if(!djConfig.isDebug){
return;
}
var _f5=arguments;
if(dj_undef("println",dojo.hostenv)){
dojo.raise("dojo.debug not available (yet?)");
}
var _f6=dj_global["jum"]&&!dj_global["jum"].isBrowser;
var s=[(_f6?"":"DEBUG: ")];
for(var i=0;i<_f5.length;++i){
if(!false&&_f5[i]&&_f5[i] instanceof Error){
var msg="["+_f5[i].name+": "+dojo.errorToString(_f5[i])+(_f5[i].fileName?", file: "+_f5[i].fileName:"")+(_f5[i].lineNumber?", line: "+_f5[i].lineNumber:"")+"]";
}else{
try{
var msg=String(_f5[i]);
}
catch(e){
if(dojo.render.html.ie){
var msg="[ActiveXObject]";
}else{
var msg="[unknown]";
}
}
}
s.push(msg);
}
dojo.hostenv.println(s.join(" "));
};
dojo.debugShallow=function(obj){
if(!djConfig.isDebug){
return;
}
dojo.debug("------------------------------------------------------------");
dojo.debug("Object: "+obj);
var _fb=[];
for(var _fc in obj){
try{
_fb.push(_fc+": "+obj[_fc]);
}
catch(E){
_fb.push(_fc+": ERROR - "+E.message);
}
}
_fb.sort();
for(var i=0;i<_fb.length;i++){
dojo.debug(_fb[i]);
}
dojo.debug("------------------------------------------------------------");
};
dojo.debugDeep=function(obj){
if(!djConfig.isDebug){
return;
}
if(!dojo.uri||!dojo.uri.dojoUri){
return dojo.debug("You'll need to load dojo.uri.* for deep debugging - sorry!");
}
if(!window.open){
return dojo.debug("Deep debugging is only supported in host environments with window.open");
}
var idx=dojo.debugDeep.debugVars.length;
dojo.debugDeep.debugVars.push(obj);
var url=new dojo.uri.Uri(location,dojo.uri.dojoUri("src/debug/deep.html?var="+idx)).toString();
var win=window.open(url,"_blank","width=600, height=400, resizable=yes, scrollbars=yes, status=yes");
try{
win.debugVar=obj;
}
catch(e){
}
};
dojo.debugDeep.debugVars=[];
if(!this["dojo"]){
alert("\"dojo/__package__.js\" is now located at \"dojo/dojo.js\". Please update your includes accordingly");
}
dojo.provide("dojo.ns");
dojo.ns={namespaces:{},failed:{},loading:{},loaded:{},register:function(name,_103,_104,_105){
if(!_105||!this.namespaces[name]){
this.namespaces[name]=new dojo.ns.Ns(name,_103,_104);
}
},allow:function(name){
if(this.failed[name]){
return false;
}
if((djConfig.excludeNamespace)&&(dojo.lang.inArray(djConfig.excludeNamespace,name))){
return false;
}
return ((name==this.dojo)||(!djConfig.includeNamespace)||(dojo.lang.inArray(djConfig.includeNamespace,name)));
},get:function(name){
return this.namespaces[name];
},require:function(name){
var ns=this.namespaces[name];
if((ns)&&(this.loaded[name])){
return ns;
}
if(!this.allow(name)){
return false;
}
if(this.loading[name]){
dojo.debug("dojo.namespace.require: re-entrant request to load namespace \""+name+"\" must fail.");
return false;
}
var req=dojo.require;
this.loading[name]=true;
try{
if(name=="dojo"){
req("dojo.namespaces.dojo");
}else{
if(!dojo.hostenv.moduleHasPrefix(name)){
dojo.registerModulePath(name,"../"+name);
}
req([name,"manifest"].join("."),false,true);
}
if(!this.namespaces[name]){
this.failed[name]=true;
}
}
finally{
this.loading[name]=false;
}
return this.namespaces[name];
}};
dojo.ns.Ns=function(name,_10c,_10d){
this.name=name;
this.module=_10c;
this.resolver=_10d;
this._loaded=[];
this._failed=[];
};
dojo.ns.Ns.prototype.resolve=function(name,_10f,_110){
if(!this.resolver||djConfig["skipAutoRequire"]){
return false;
}
var _111=this.resolver(name,_10f);
if((_111)&&(!this._loaded[_111])&&(!this._failed[_111])){
var req=dojo.require;
req(_111,false,true);
if(dojo.hostenv.findModule(_111,false)){
this._loaded[_111]=true;
}else{
if(!_110){
dojo.raise("dojo.ns.Ns.resolve: module '"+_111+"' not found after loading via namespace '"+this.name+"'");
}
this._failed[_111]=true;
}
}
return Boolean(this._loaded[_111]);
};
dojo.registerNamespace=function(name,_114,_115){
dojo.ns.register.apply(dojo.ns,arguments);
};
dojo.registerNamespaceResolver=function(name,_117){
var n=dojo.ns.namespaces[name];
if(n){
n.resolver=_117;
}
};
dojo.registerNamespaceManifest=function(_119,path,name,_11c,_11d){
dojo.registerModulePath(name,path);
dojo.registerNamespace(name,_11c,_11d);
};
dojo.registerNamespace("dojo","dojo.widget");
dojo.provide("dojo.namespaces.dojo");
(function(){
var map={html:{"accordioncontainer":"dojo.widget.AccordionContainer","animatedpng":"dojo.widget.AnimatedPng","button":"dojo.widget.Button","chart":"dojo.widget.Chart","checkbox":"dojo.widget.Checkbox","clock":"dojo.widget.Clock","colorpalette":"dojo.widget.ColorPalette","combobox":"dojo.widget.ComboBox","combobutton":"dojo.widget.Button","contentpane":"dojo.widget.ContentPane","currencytextbox":"dojo.widget.CurrencyTextbox","datepicker":"dojo.widget.DatePicker","datetextbox":"dojo.widget.DateTextbox","debugconsole":"dojo.widget.DebugConsole","dialog":"dojo.widget.Dialog","dropdownbutton":"dojo.widget.Button","dropdowndatepicker":"dojo.widget.DropdownDatePicker","dropdowntimepicker":"dojo.widget.DropdownTimePicker","emaillisttextbox":"dojo.widget.InternetTextbox","emailtextbox":"dojo.widget.InternetTextbox","editor":"dojo.widget.Editor","editor2":"dojo.widget.Editor2","filteringtable":"dojo.widget.FilteringTable","fisheyelist":"dojo.widget.FisheyeList","fisheyelistitem":"dojo.widget.FisheyeList","floatingpane":"dojo.widget.FloatingPane","modalfloatingpane":"dojo.widget.FloatingPane","form":"dojo.widget.Form","googlemap":"dojo.widget.GoogleMap","inlineeditbox":"dojo.widget.InlineEditBox","integerspinner":"dojo.widget.Spinner","integertextbox":"dojo.widget.IntegerTextbox","ipaddresstextbox":"dojo.widget.InternetTextbox","layoutcontainer":"dojo.widget.LayoutContainer","linkpane":"dojo.widget.LinkPane","popupmenu2":"dojo.widget.Menu2","menuitem2":"dojo.widget.Menu2","menuseparator2":"dojo.widget.Menu2","menubar2":"dojo.widget.Menu2","menubaritem2":"dojo.widget.Menu2","pagecontainer":"dojo.widget.PageContainer","pagecontroller":"dojo.widget.PageContainer","popupcontainer":"dojo.widget.PopupContainer","progressbar":"dojo.widget.ProgressBar","radiogroup":"dojo.widget.RadioGroup","realnumbertextbox":"dojo.widget.RealNumberTextbox","regexptextbox":"dojo.widget.RegexpTextbox","repeater":"dojo.widget.Repeater","resizabletextarea":"dojo.widget.ResizableTextarea","richtext":"dojo.widget.RichText","select":"dojo.widget.Select","show":"dojo.widget.Show","showaction":"dojo.widget.ShowAction","showslide":"dojo.widget.ShowSlide","slidervertical":"dojo.widget.Slider","sliderhorizontal":"dojo.widget.Slider","slider":"dojo.widget.Slider","slideshow":"dojo.widget.SlideShow","sortabletable":"dojo.widget.SortableTable","splitcontainer":"dojo.widget.SplitContainer","tabcontainer":"dojo.widget.TabContainer","tabcontroller":"dojo.widget.TabContainer","taskbar":"dojo.widget.TaskBar","textbox":"dojo.widget.Textbox","timepicker":"dojo.widget.TimePicker","timetextbox":"dojo.widget.DateTextbox","titlepane":"dojo.widget.TitlePane","toaster":"dojo.widget.Toaster","toggler":"dojo.widget.Toggler","toolbar":"dojo.widget.Toolbar","toolbarcontainer":"dojo.widget.Toolbar","toolbaritem":"dojo.widget.Toolbar","toolbarbuttongroup":"dojo.widget.Toolbar","toolbarbutton":"dojo.widget.Toolbar","toolbardialog":"dojo.widget.Toolbar","toolbarmenu":"dojo.widget.Toolbar","toolbarseparator":"dojo.widget.Toolbar","toolbarspace":"dojo.widget.Toolbar","toolbarselect":"dojo.widget.Toolbar","toolbarcolordialog":"dojo.widget.Toolbar","tooltip":"dojo.widget.Tooltip","tree":"dojo.widget.Tree","treebasiccontroller":"dojo.widget.TreeBasicController","treecontextmenu":"dojo.widget.TreeContextMenu","treedisablewrapextension":"dojo.widget.TreeDisableWrapExtension","treedociconextension":"dojo.widget.TreeDocIconExtension","treeeditor":"dojo.widget.TreeEditor","treeemphasizeonselect":"dojo.widget.TreeEmphasizeOnSelect","treeexpandtonodeonselect":"dojo.widget.TreeExpandToNodeOnSelect","treelinkextension":"dojo.widget.TreeLinkExtension","treeloadingcontroller":"dojo.widget.TreeLoadingController","treemenuitem":"dojo.widget.TreeContextMenu","treenode":"dojo.widget.TreeNode","treerpccontroller":"dojo.widget.TreeRPCController","treeselector":"dojo.widget.TreeSelector","treetoggleonselect":"dojo.widget.TreeToggleOnSelect","treev3":"dojo.widget.TreeV3","treebasiccontrollerv3":"dojo.widget.TreeBasicControllerV3","treecontextmenuv3":"dojo.widget.TreeContextMenuV3","treedndcontrollerv3":"dojo.widget.TreeDndControllerV3","treeloadingcontrollerv3":"dojo.widget.TreeLoadingControllerV3","treemenuitemv3":"dojo.widget.TreeContextMenuV3","treerpccontrollerv3":"dojo.widget.TreeRpcControllerV3","treeselectorv3":"dojo.widget.TreeSelectorV3","urltextbox":"dojo.widget.InternetTextbox","usphonenumbertextbox":"dojo.widget.UsTextbox","ussocialsecuritynumbertextbox":"dojo.widget.UsTextbox","usstatetextbox":"dojo.widget.UsTextbox","usziptextbox":"dojo.widget.UsTextbox","validationtextbox":"dojo.widget.ValidationTextbox","treeloadingcontroller":"dojo.widget.TreeLoadingController","wizardcontainer":"dojo.widget.Wizard","wizardpane":"dojo.widget.Wizard","yahoomap":"dojo.widget.YahooMap"},svg:{"chart":"dojo.widget.svg.Chart"},vml:{"chart":"dojo.widget.vml.Chart"}};
dojo.addDojoNamespaceMapping=function(_11f,_120){
map[_11f]=_120;
};
function dojoNamespaceResolver(name,_122){
if(!_122){
_122="html";
}
if(!map[_122]){
return null;
}
return map[_122][name];
}
dojo.registerNamespaceResolver("dojo",dojoNamespaceResolver);
})();
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_123,_124){
if(!dojo.lang.isFunction(_124)){
dojo.raise("dojo.inherits: superclass argument ["+_124+"] must be a function (subclass: ["+_123+"']");
}
_123.prototype=new _124();
_123.prototype.constructor=_123;
_123.superclass=_124.prototype;
_123["super"]=_124.prototype;
};
dojo.lang._mixin=function(obj,_126){
var tobj={};
for(var x in _126){
if((typeof tobj[x]=="undefined")||(tobj[x]!=_126[x])){
obj[x]=_126[x];
}
}
if(dojo.render.html.ie&&(typeof (_126["toString"])=="function")&&(_126["toString"]!=obj["toString"])&&(_126["toString"]!=tobj["toString"])){
obj.toString=_126.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_12a){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_12d,_12e){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_12d.prototype,arguments[i]);
}
return _12d;
};
dojo.lang._delegate=function(obj){
function TMP(){
}
TMP.prototype=obj;
return new TMP();
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_132,_133,_134,_135){
if(!dojo.lang.isArrayLike(_132)&&dojo.lang.isArrayLike(_133)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_132;
_132=_133;
_133=temp;
}
var _137=dojo.lang.isString(_132);
if(_137){
_132=_132.split("");
}
if(_135){
var step=-1;
var i=_132.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_132.length;
}
if(_134){
while(i!=end){
if(_132[i]===_133){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_132[i]==_133){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_13b,_13c,_13d){
return dojo.lang.find(_13b,_13c,_13d,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_13e,_13f){
return dojo.lang.find(_13e,_13f)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
return (it instanceof Function||typeof it=="function");
};
(function(){
if((dojo.render.html.capable)&&(dojo.render.html["safari"])){
dojo.lang.isFunction=function(it){
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
}
})();
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction(it)&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_14a,_14b){
var fcn=(dojo.lang.isString(_14b)?_14a[_14b]:_14b)||function(){
};
return function(){
return fcn.apply(_14a,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_14d,_14e,_14f){
var nso=(_14e||dojo.lang.anon);
if((_14f)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_14d){
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_14d;
return ret;
};
dojo.lang.forward=function(_153){
return function(){
return this[_153].apply(this,arguments);
};
};
dojo.lang.curry=function(_154,func){
var _156=[];
_154=_154||dj_global;
if(dojo.lang.isString(func)){
func=_154[func];
}
for(var x=2;x<arguments.length;x++){
_156.push(arguments[x]);
}
var _158=(func["__preJoinArity"]||func.length)-_156.length;
function gather(_159,_15a,_15b){
var _15c=_15b;
var _15d=_15a.slice(0);
for(var x=0;x<_159.length;x++){
_15d.push(_159[x]);
}
_15b=_15b-_159.length;
if(_15b<=0){
var res=func.apply(_154,_15d);
_15b=_15c;
return res;
}else{
return function(){
return gather(arguments,_15d,_15b);
};
}
}
return gather([],_156,_158);
};
dojo.lang.curryArguments=function(_160,func,args,_163){
var _164=[];
var x=_163||0;
for(x=_163;x<args.length;x++){
_164.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[_160,func].concat(_164));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_16a,_16b){
if(!farr.length){
if(typeof _16b=="function"){
_16b();
}
return;
}
if((typeof _16a=="undefined")&&(typeof cb=="number")){
_16a=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_16a){
_16a=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_16a,_16b);
},_16a);
};
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_16d){
var _16e=window,_16f=2;
if(!dojo.lang.isFunction(func)){
_16e=func;
func=_16d;
_16d=arguments[2];
_16f++;
}
if(dojo.lang.isString(func)){
func=_16e[func];
}
var args=[];
for(var i=_16f;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_16e,args);
},_16d);
};
dojo.lang.clearTimeout=function(_172){
dojo.global().clearTimeout(_172);
};
dojo.lang.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.lang.shallowCopy=function(obj,deep){
var i,ret;
if(obj===null){
return null;
}
if(dojo.lang.isObject(obj)){
ret=new obj.constructor();
for(i in obj){
if(dojo.lang.isUndefined(ret[i])){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}
}else{
if(dojo.lang.isArray(obj)){
ret=[];
for(i=0;i<obj.length;i++){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}else{
ret=obj;
}
}
return ret;
};
dojo.lang.firstValued=function(){
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]!="undefined"){
return arguments[i];
}
}
return undefined;
};
dojo.lang.getObjPathValue=function(_17b,_17c,_17d){
with(dojo.parseObjPath(_17b,_17c,_17d)){
return dojo.evalProp(prop,obj,_17d);
}
};
dojo.lang.setObjPathValue=function(_17e,_17f,_180,_181){
dojo.deprecated("dojo.lang.setObjPathValue","use dojo.parseObjPath and the '=' operator","0.6");
if(arguments.length<4){
_181=true;
}
with(dojo.parseObjPath(_17e,_180,_181)){
if(obj&&(_181||(prop in obj))){
obj[prop]=_17f;
}
}
};
dojo.provide("dojo.lang.declare");
dojo.lang.declare=function(_182,_183,init,_185){
if((dojo.lang.isFunction(_185))||((!_185)&&(!dojo.lang.isFunction(init)))){
var temp=_185;
_185=init;
init=temp;
}
var _187=[];
if(dojo.lang.isArray(_183)){
_187=_183;
_183=_187.shift();
}
if(!init){
init=dojo.evalObjPath(_182,false);
if((init)&&(!dojo.lang.isFunction(init))){
init=null;
}
}
var ctor=dojo.lang.declare._makeConstructor();
var scp=(_183?_183.prototype:null);
if(scp){
scp.prototyping=true;
ctor.prototype=new _183();
scp.prototyping=false;
}
ctor.superclass=scp;
ctor.mixins=_187;
for(var i=0,l=_187.length;i<l;i++){
dojo.lang.extend(ctor,_187[i].prototype);
}
ctor.prototype.initializer=null;
ctor.prototype.declaredClass=_182;
if(dojo.lang.isArray(_185)){
dojo.lang.extend.apply(dojo.lang,[ctor].concat(_185));
}else{
dojo.lang.extend(ctor,(_185)||{});
}
dojo.lang.extend(ctor,dojo.lang.declare._common);
ctor.prototype.constructor=ctor;
ctor.prototype.initializer=(ctor.prototype.initializer)||(init)||(function(){
});
var _18c=dojo.parseObjPath(_182,null,true);
_18c.obj[_18c.prop]=ctor;
return ctor;
};
dojo.lang.declare._makeConstructor=function(){
return function(){
var self=this._getPropContext();
var s=self.constructor.superclass;
if((s)&&(s.constructor)){
if(s.constructor==arguments.callee){
this._inherited("constructor",arguments);
}else{
this._contextMethod(s,"constructor",arguments);
}
}
var ms=(self.constructor.mixins)||([]);
for(var i=0,m;(m=ms[i]);i++){
(((m.prototype)&&(m.prototype.initializer))||(m)).apply(this,arguments);
}
if((!this.prototyping)&&(self.initializer)){
self.initializer.apply(this,arguments);
}
};
};
dojo.lang.declare._common={_getPropContext:function(){
return (this.___proto||this);
},_contextMethod:function(_192,_193,args){
var _195,_196=this.___proto;
this.___proto=_192;
try{
_195=_192[_193].apply(this,(args||[]));
}
catch(e){
throw e;
}
finally{
this.___proto=_196;
}
return _195;
},_inherited:function(prop,args){
var p=this._getPropContext();
do{
if((!p.constructor)||(!p.constructor.superclass)){
return;
}
p=p.constructor.superclass;
}while(!(prop in p));
return (dojo.lang.isFunction(p[prop])?this._contextMethod(p,prop,args):p[prop]);
},inherited:function(prop,args){
dojo.deprecated("'inherited' method is dangerous, do not up-call! 'inherited' is slated for removal in 0.5; name your super class (or use superclass property) instead.","0.5");
this._inherited(prop,args);
}};
dojo.declare=dojo.lang.declare;
dojo.provide("dojo.dnd.DragAndDrop");
dojo.declare("dojo.dnd.DragSource",null,{type:"",onDragEnd:function(evt){
},onDragStart:function(evt){
},onSelected:function(evt){
},unregister:function(){
dojo.dnd.dragManager.unregisterDragSource(this);
},reregister:function(){
dojo.dnd.dragManager.registerDragSource(this);
}});
dojo.declare("dojo.dnd.DragObject",null,{type:"",register:function(){
var dm=dojo.dnd.dragManager;
if(dm["registerDragObject"]){
dm.registerDragObject(this);
}
},onDragStart:function(evt){
},onDragMove:function(evt){
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragEnd:function(evt){
},onDragLeave:dojo.lang.forward("onDragOut"),onDragEnter:dojo.lang.forward("onDragOver"),ondragout:dojo.lang.forward("onDragOut"),ondragover:dojo.lang.forward("onDragOver")});
dojo.declare("dojo.dnd.DropTarget",null,{acceptsType:function(type){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
if(!dojo.lang.inArray(this.acceptedTypes,type)){
return false;
}
}
return true;
},accepts:function(_1a6){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
for(var i=0;i<_1a6.length;i++){
if(!dojo.lang.inArray(this.acceptedTypes,_1a6[i].type)){
return false;
}
}
}
return true;
},unregister:function(){
dojo.dnd.dragManager.unregisterDropTarget(this);
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragMove:function(evt){
},onDropStart:function(evt){
},onDrop:function(evt){
},onDropEnd:function(){
}},function(){
this.acceptedTypes=[];
});
dojo.dnd.DragEvent=function(){
this.dragSource=null;
this.dragObject=null;
this.target=null;
this.eventStatus="success";
};
dojo.declare("dojo.dnd.DragManager",null,{selectedSources:[],dragObjects:[],dragSources:[],registerDragSource:function(_1ad){
},dropTargets:[],registerDropTarget:function(_1ae){
},lastDragTarget:null,currentDragTarget:null,onKeyDown:function(){
},onMouseOut:function(){
},onMouseMove:function(){
},onMouseUp:function(){
}});
dojo.provide("dojo.lang.array");
dojo.lang.mixin(dojo.lang,{has:function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
},isEmpty:function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _1b3=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_1b3++;
break;
}
}
return _1b3==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
},map:function(arr,obj,_1b7){
var _1b8=dojo.lang.isString(arr);
if(_1b8){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_1b7)){
_1b7=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_1b7){
var _1b9=obj;
obj=_1b7;
_1b7=_1b9;
}
}
if(Array.map){
var _1ba=Array.map(arr,_1b7,obj);
}else{
var _1ba=[];
for(var i=0;i<arr.length;++i){
_1ba.push(_1b7.call(obj,arr[i]));
}
}
if(_1b8){
return _1ba.join("");
}else{
return _1ba;
}
},reduce:function(arr,_1bd,obj,_1bf){
var _1c0=_1bd;
if(arguments.length==1){
dojo.debug("dojo.lang.reduce called with too few arguments!");
return false;
}else{
if(arguments.length==2){
_1bf=_1bd;
_1c0=arr.shift();
}else{
if(arguments.lenght==3){
if(dojo.lang.isFunction(obj)){
_1bf=obj;
obj=null;
}
}else{
if(dojo.lang.isFunction(obj)){
var tmp=_1bf;
_1bf=obj;
obj=tmp;
}
}
}
}
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_1c0=_1bf.call(ob,_1c0,val);
});
return _1c0;
},forEach:function(_1c4,_1c5,_1c6){
if(dojo.lang.isString(_1c4)){
_1c4=_1c4.split("");
}
if(Array.forEach){
Array.forEach(_1c4,_1c5,_1c6);
}else{
if(!_1c6){
_1c6=dj_global;
}
for(var i=0,l=_1c4.length;i<l;i++){
_1c5.call(_1c6,_1c4[i],i,_1c4);
}
}
},_everyOrSome:function(_1c9,arr,_1cb,_1cc){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_1c9?"every":"some"](arr,_1cb,_1cc);
}else{
if(!_1cc){
_1cc=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _1cf=_1cb.call(_1cc,arr[i],i,arr);
if(_1c9&&!_1cf){
return false;
}else{
if((!_1c9)&&(_1cf)){
return true;
}
}
}
return Boolean(_1c9);
}
},every:function(arr,_1d1,_1d2){
return this._everyOrSome(true,arr,_1d1,_1d2);
},some:function(arr,_1d4,_1d5){
return this._everyOrSome(false,arr,_1d4,_1d5);
},filter:function(arr,_1d7,_1d8){
var _1d9=dojo.lang.isString(arr);
if(_1d9){
arr=arr.split("");
}
var _1da;
if(Array.filter){
_1da=Array.filter(arr,_1d7,_1d8);
}else{
if(!_1d8){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_1d8=dj_global;
}
_1da=[];
for(var i=0;i<arr.length;i++){
if(_1d7.call(_1d8,arr[i],i,arr)){
_1da.push(arr[i]);
}
}
}
if(_1d9){
return _1da.join("");
}else{
return _1da;
}
},unnest:function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
},toArray:function(_1df,_1e0){
var _1e1=[];
for(var i=_1e0||0;i<_1df.length;i++){
_1e1.push(_1df[i]);
}
return _1e1;
}});
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_1e4){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false,maxCalls:-1};
switch(args.length){
case 0:
return;
case 1:
return;
case 2:
ao.srcFunc=args[0];
ao.adviceFunc=args[1];
break;
case 3:
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isFunction(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
var _1e7=dl.nameAnonFunc(args[2],ao.adviceObj,_1e4);
ao.adviceFunc=_1e7;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _1e7=dl.nameAnonFunc(args[0],ao.srcObj,_1e4);
ao.srcFunc=_1e7;
ao.adviceObj=args[1];
ao.adviceFunc=args[2];
}
}
}
}
break;
case 4:
if((dl.isObject(args[0]))&&(dl.isObject(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isString(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isFunction(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
var _1e7=dl.nameAnonFunc(args[1],dj_global,_1e4);
ao.srcFunc=_1e7;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _1e7=dl.nameAnonFunc(args[3],dj_global,_1e4);
ao.adviceObj=dj_global;
ao.adviceFunc=_1e7;
}else{
if(dl.isObject(args[1])){
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=dj_global;
ao.adviceFunc=args[3];
}else{
if(dl.isObject(args[2])){
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
ao.srcObj=ao.adviceObj=ao.aroundObj=dj_global;
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
ao.aroundFunc=args[3];
}
}
}
}
}
}
break;
case 6:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundFunc=args[5];
ao.aroundObj=dj_global;
break;
default:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundObj=args[5];
ao.aroundFunc=args[6];
ao.once=args[7];
ao.delay=args[8];
ao.rate=args[9];
ao.adviceMsg=args[10];
ao.maxCalls=(!isNaN(parseInt(args[11])))?args[11]:-1;
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _1e7=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_1e4);
ao.aroundFunc=_1e7;
}
if(dl.isFunction(ao.srcFunc)){
ao.srcFunc=dl.getNameInObj(ao.srcObj,ao.srcFunc);
}
if(dl.isFunction(ao.adviceFunc)){
ao.adviceFunc=dl.getNameInObj(ao.adviceObj,ao.adviceFunc);
}
if((ao.aroundObj)&&(dl.isFunction(ao.aroundFunc))){
ao.aroundFunc=dl.getNameInObj(ao.aroundObj,ao.aroundFunc);
}
if(!ao.srcObj){
dojo.raise("bad srcObj for srcFunc: "+ao.srcFunc);
}
if(!ao.adviceObj){
dojo.raise("bad adviceObj for adviceFunc: "+ao.adviceFunc);
}
if(!ao.adviceFunc){
dojo.debug("bad adviceFunc for srcFunc: "+ao.srcFunc);
dojo.debugShallow(ao);
}
return ao;
}
this.connect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.connect(ao);
}
ao.srcFunc="onkeypress";
}
if(dojo.lang.isArray(ao.srcObj)&&ao.srcObj!=""){
var _1e9={};
for(var x in ao){
_1e9[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_1e9.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_1e9));
});
return mjps;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
if(ao.adviceFunc){
var mjp2=dojo.event.MethodJoinPoint.getForMethod(ao.adviceObj,ao.adviceFunc);
}
mjp.kwAddAdvice(ao);
return mjp;
};
this.log=function(a1,a2){
var _1f1;
if((arguments.length==1)&&(typeof a1=="object")){
_1f1=a1;
}else{
_1f1={srcObj:a1,srcFunc:a2};
}
_1f1.adviceFunc=function(){
var _1f2=[];
for(var x=0;x<arguments.length;x++){
_1f2.push(arguments[x]);
}
dojo.debug("("+_1f1.srcObj+")."+_1f1.srcFunc,":",_1f2.join(", "));
};
this.kwConnect(_1f1);
};
this.connectBefore=function(){
var args=["before"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectAround=function(){
var args=["around"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.once=true;
return this.connect(ao);
};
this.connectRunOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.maxCalls=1;
return this.connect(ao);
};
this._kwConnectImpl=function(_1fa,_1fb){
var fn=(_1fb)?"disconnect":"connect";
if(typeof _1fa["srcFunc"]=="function"){
_1fa.srcObj=_1fa["srcObj"]||dj_global;
var _1fd=dojo.lang.nameAnonFunc(_1fa.srcFunc,_1fa.srcObj,true);
_1fa.srcFunc=_1fd;
}
if(typeof _1fa["adviceFunc"]=="function"){
_1fa.adviceObj=_1fa["adviceObj"]||dj_global;
var _1fd=dojo.lang.nameAnonFunc(_1fa.adviceFunc,_1fa.adviceObj,true);
_1fa.adviceFunc=_1fd;
}
_1fa.srcObj=_1fa["srcObj"]||dj_global;
_1fa.adviceObj=_1fa["adviceObj"]||_1fa["targetObj"]||dj_global;
_1fa.adviceFunc=_1fa["adviceFunc"]||_1fa["targetFunc"];
return dojo.event[fn](_1fa);
};
this.kwConnect=function(_1fe){
return this._kwConnectImpl(_1fe,false);
};
this.disconnect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(!ao.adviceFunc){
return;
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.disconnect(ao);
}
ao.srcFunc="onkeypress";
}
if(!ao.srcObj[ao.srcFunc]){
return null;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc,true);
mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
return mjp;
};
this.kwDisconnect=function(_201){
return this._kwConnectImpl(_201,true);
};
};
dojo.event.MethodInvocation=function(_202,obj,args){
this.jp_=_202;
this.object=obj;
this.args=[];
for(var x=0;x<args.length;x++){
this.args[x]=args[x];
}
this.around_index=-1;
};
dojo.event.MethodInvocation.prototype.proceed=function(){
this.around_index++;
if(this.around_index>=this.jp_.around.length){
return this.jp_.object[this.jp_.methodname].apply(this.jp_.object,this.args);
}else{
var ti=this.jp_.around[this.around_index];
var mobj=ti[0]||dj_global;
var meth=ti[1];
return mobj[meth].call(mobj,this);
}
};
dojo.event.MethodJoinPoint=function(obj,_20a){
this.object=obj||dj_global;
this.methodname=_20a;
this.methodfunc=this.object[_20a];
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_20c){
if(!obj){
obj=dj_global;
}
var ofn=obj[_20c];
if(!ofn){
ofn=obj[_20c]=function(){
};
if(!obj[_20c]){
dojo.raise("Cannot set do-nothing method on that object "+_20c);
}
}else{
if((typeof ofn!="function")&&(!dojo.lang.isFunction(ofn))&&(!dojo.lang.isAlien(ofn))){
return null;
}
}
var _20e=_20c+"$joinpoint";
var _20f=_20c+"$joinpoint$method";
var _210=obj[_20e];
if(!_210){
var _211=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_211=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_20e,_20f,_20c]);
}
}
var _212=ofn.length;
obj[_20f]=ofn;
_210=obj[_20e]=new dojo.event.MethodJoinPoint(obj,_20f);
if(!_211){
obj[_20c]=function(){
return _210.run.apply(_210,arguments);
};
}else{
obj[_20c]=function(){
var args=[];
if(!arguments.length){
var evt=null;
try{
if(obj.ownerDocument){
evt=obj.ownerDocument.parentWindow.event;
}else{
if(obj.documentElement){
evt=obj.documentElement.ownerDocument.parentWindow.event;
}else{
if(obj.event){
evt=obj.event;
}else{
evt=window.event;
}
}
}
}
catch(e){
evt=window.event;
}
if(evt){
args.push(dojo.event.browser.fixEvent(evt,this));
}
}else{
for(var x=0;x<arguments.length;x++){
if((x==0)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _210.run.apply(_210,args);
};
}
obj[_20c].__preJoinArity=_212;
}
return _210;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{squelch:false,unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _218=[];
for(var x=0;x<args.length;x++){
_218[x]=args[x];
}
var _21a=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _21c=marr[0]||dj_global;
var _21d=marr[1];
if(!_21c[_21d]){
dojo.raise("function \""+_21d+"\" does not exist on \""+_21c+"\"");
}
var _21e=marr[2]||dj_global;
var _21f=marr[3];
var msg=marr[6];
var _221=marr[7];
if(_221>-1){
if(_221==0){
return;
}
marr[7]--;
}
var _222;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _21c[_21d].apply(_21c,to.args);
}};
to.args=_218;
var _224=parseInt(marr[4]);
var _225=((!isNaN(_224))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _228=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_21a(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_21f){
_21e[_21f].call(_21e,to);
}else{
if((_225)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_21c[_21d].call(_21c,to);
}else{
_21c[_21d].apply(_21c,args);
}
},_224);
}else{
if(msg){
_21c[_21d].call(_21c,to);
}else{
_21c[_21d].apply(_21c,args);
}
}
}
};
var _22b=function(){
if(this.squelch){
try{
return _21a.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _21a.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_22b);
}
var _22c;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_22c=mi.proceed();
}else{
if(this.methodfunc){
_22c=this.object[this.methodname].apply(this.object,args);
}
}
}
catch(e){
if(!this.squelch){
dojo.debug(e,"when calling",this.methodname,"on",this.object,"with arguments",args);
dojo.raise(e);
}
}
if((this["after"])&&(this.after.length>0)){
dojo.lang.forEach(this.after.concat(new Array()),_22b);
}
return (this.methodfunc)?_22c:null;
},getArr:function(kind){
var type="after";
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
type="before";
}else{
if(kind=="around"){
type="around";
}
}
if(!this[type]){
this[type]=[];
}
return this[type];
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"],args["maxCalls"]);
},addAdvice:function(_231,_232,_233,_234,_235,_236,once,_238,rate,_23a,_23b){
var arr=this.getArr(_235);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_231,_232,_233,_234,_238,rate,_23a,_23b];
if(once){
if(this.hasAdvice(_231,_232,_235,arr)>=0){
return;
}
}
if(_236=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_23e,_23f,_240,arr){
if(!arr){
arr=this.getArr(_240);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _23f=="object")?(new String(_23f)).toString():_23f;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_23e)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_246,_247,_248,once){
var arr=this.getArr(_248);
var ind=this.hasAdvice(_246,_247,_248,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_246,_247,_248,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_24c){
if(!this.topics[_24c]){
this.topics[_24c]=new this.TopicImpl(_24c);
}
return this.topics[_24c];
};
this.registerPublisher=function(_24d,obj,_24f){
var _24d=this.getTopic(_24d);
_24d.registerPublisher(obj,_24f);
};
this.subscribe=function(_250,obj,_252){
var _250=this.getTopic(_250);
_250.subscribe(obj,_252);
};
this.unsubscribe=function(_253,obj,_255){
var _253=this.getTopic(_253);
_253.unsubscribe(obj,_255);
};
this.destroy=function(_256){
this.getTopic(_256).destroy();
delete this.topics[_256];
};
this.publishApply=function(_257,args){
var _257=this.getTopic(_257);
_257.sendMessage.apply(_257,args);
};
this.publish=function(_259,_25a){
var _259=this.getTopic(_259);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_259.sendMessage.apply(_259,args);
};
};
dojo.event.topic.TopicImpl=function(_25d){
this.topicName=_25d;
this.subscribe=function(_25e,_25f){
var tf=_25f||_25e;
var to=(!_25f)?dj_global:_25e;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_262,_263){
var tf=(!_263)?_262:_263;
var to=(!_263)?null:_262;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_266){
this._getJoinPoint().squelch=_266;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_267,_268){
dojo.event.connect(_267,_268,this,"sendMessage");
};
this.sendMessage=function(_269){
};
};
dojo.provide("dojo.event.browser");
dojo._ie_clobber=new function(){
this.clobberNodes=[];
function nukeProp(node,prop){
try{
node[prop]=null;
}
catch(e){
}
try{
delete node[prop];
}
catch(e){
}
try{
node.removeAttribute(prop);
}
catch(e){
}
}
this.clobber=function(_26c){
var na;
var tna;
if(_26c){
tna=_26c.all||_26c.getElementsByTagName("*");
na=[_26c];
for(var x=0;x<tna.length;x++){
if(tna[x]["__doClobber__"]){
na.push(tna[x]);
}
}
}else{
try{
window.onload=null;
}
catch(e){
}
na=(this.clobberNodes.length)?this.clobberNodes:document.all;
}
tna=null;
var _270={};
for(var i=na.length-1;i>=0;i=i-1){
var el=na[i];
try{
if(el&&el["__clobberAttrs__"]){
for(var j=0;j<el.__clobberAttrs__.length;j++){
nukeProp(el,el.__clobberAttrs__[j]);
}
nukeProp(el,"__clobberAttrs__");
nukeProp(el,"__doClobber__");
}
}
catch(e){
}
}
na=null;
};
};
if(dojo.render.html.ie){
dojo.addOnUnload(function(){
dojo._ie_clobber.clobber();
try{
if((dojo["widget"])&&(dojo.widget["manager"])){
dojo.widget.manager.destroyAll();
}
}
catch(e){
}
if(dojo.widget){
for(var name in dojo.widget._templateCache){
if(dojo.widget._templateCache[name].node){
dojo.dom.destroyNode(dojo.widget._templateCache[name].node);
dojo.widget._templateCache[name].node=null;
delete dojo.widget._templateCache[name].node;
}
}
}
try{
window.onload=null;
}
catch(e){
}
try{
window.onunload=null;
}
catch(e){
}
dojo._ie_clobber.clobberNodes=[];
});
}
dojo.event.browser=new function(){
var _275=0;
this.normalizedEventName=function(_276){
switch(_276){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _276;
break;
default:
return _276.toLowerCase();
break;
}
};
this.clean=function(node){
if(dojo.render.html.ie){
dojo._ie_clobber.clobber(node);
}
};
this.addClobberNode=function(node){
if(!dojo.render.html.ie){
return;
}
if(!node["__doClobber__"]){
node.__doClobber__=true;
dojo._ie_clobber.clobberNodes.push(node);
node.__clobberAttrs__=[];
}
};
this.addClobberNodeAttrs=function(node,_27a){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_27a.length;x++){
node.__clobberAttrs__.push(_27a[x]);
}
};
this.removeListener=function(node,_27d,fp,_27f){
if(!_27f){
var _27f=false;
}
_27d=dojo.event.browser.normalizedEventName(_27d);
if((_27d=="onkey")||(_27d=="key")){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_27f);
}
_27d="onkeypress";
}
if(_27d.substr(0,2)=="on"){
_27d=_27d.substr(2);
}
if(node.removeEventListener){
node.removeEventListener(_27d,fp,_27f);
}
};
this.addListener=function(node,_281,fp,_283,_284){
if(!node){
return;
}
if(!_283){
var _283=false;
}
_281=dojo.event.browser.normalizedEventName(_281);
if((_281=="onkey")||(_281=="key")){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_283,_284);
}
_281="onkeypress";
}
if(_281.substr(0,2)!="on"){
_281="on"+_281;
}
if(!_284){
var _285=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_283){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_285=fp;
}
if(node.addEventListener){
node.addEventListener(_281.substr(2),_285,_283);
return _285;
}else{
if(typeof node[_281]=="function"){
var _288=node[_281];
node[_281]=function(e){
_288(e);
return _285(e);
};
}else{
node[_281]=_285;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_281]);
}
return _285;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(obj)&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_28b,_28c){
if(typeof _28b!="function"){
dojo.raise("listener not a function: "+_28b);
}
dojo.event.browser.currentEvent.currentTarget=_28c;
return _28b.call(_28c,dojo.event.browser.currentEvent);
};
this._stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this._preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_28f){
if(!evt){
if(window["event"]){
evt=window.event;
}
}
if((evt["type"])&&(evt["type"].indexOf("key")==0)){
evt.keys=this.revKeys;
for(var key in this.keys){
evt[key]=this.keys[key];
}
if(evt["type"]=="keydown"&&dojo.render.html.ie){
switch(evt.keyCode){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_LEFT_WINDOW:
case evt.KEY_RIGHT_WINDOW:
case evt.KEY_SELECT:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
case evt.KEY_NUMPAD_0:
case evt.KEY_NUMPAD_1:
case evt.KEY_NUMPAD_2:
case evt.KEY_NUMPAD_3:
case evt.KEY_NUMPAD_4:
case evt.KEY_NUMPAD_5:
case evt.KEY_NUMPAD_6:
case evt.KEY_NUMPAD_7:
case evt.KEY_NUMPAD_8:
case evt.KEY_NUMPAD_9:
case evt.KEY_NUMPAD_PERIOD:
break;
case evt.KEY_NUMPAD_MULTIPLY:
case evt.KEY_NUMPAD_PLUS:
case evt.KEY_NUMPAD_ENTER:
case evt.KEY_NUMPAD_MINUS:
case evt.KEY_NUMPAD_DIVIDE:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
case evt.KEY_PAGE_UP:
case evt.KEY_PAGE_DOWN:
case evt.KEY_END:
case evt.KEY_HOME:
case evt.KEY_LEFT_ARROW:
case evt.KEY_UP_ARROW:
case evt.KEY_RIGHT_ARROW:
case evt.KEY_DOWN_ARROW:
case evt.KEY_INSERT:
case evt.KEY_DELETE:
case evt.KEY_F1:
case evt.KEY_F2:
case evt.KEY_F3:
case evt.KEY_F4:
case evt.KEY_F5:
case evt.KEY_F6:
case evt.KEY_F7:
case evt.KEY_F8:
case evt.KEY_F9:
case evt.KEY_F10:
case evt.KEY_F11:
case evt.KEY_F12:
case evt.KEY_F12:
case evt.KEY_F13:
case evt.KEY_F14:
case evt.KEY_F15:
case evt.KEY_CLEAR:
case evt.KEY_HELP:
evt.key=evt.keyCode;
break;
default:
if(evt.ctrlKey||evt.altKey){
var _291=evt.keyCode;
if(_291>=65&&_291<=90&&evt.shiftKey==false){
_291+=32;
}
if(_291>=1&&_291<=26&&evt.ctrlKey){
_291+=96;
}
evt.key=String.fromCharCode(_291);
}
}
}else{
if(evt["type"]=="keypress"){
if(dojo.render.html.opera){
if(evt.which==0){
evt.key=evt.keyCode;
}else{
if(evt.which>0){
switch(evt.which){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
evt.key=evt.which;
break;
default:
var _291=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_291+=32;
}
evt.key=String.fromCharCode(_291);
}
}
}
}else{
if(dojo.render.html.ie){
if(!evt.ctrlKey&&!evt.altKey&&evt.keyCode>=evt.KEY_SPACE){
evt.key=String.fromCharCode(evt.keyCode);
}
}else{
if(dojo.render.html.safari){
switch(evt.keyCode){
case 25:
evt.key=evt.KEY_TAB;
evt.shift=true;
break;
case 63232:
evt.key=evt.KEY_UP_ARROW;
break;
case 63233:
evt.key=evt.KEY_DOWN_ARROW;
break;
case 63234:
evt.key=evt.KEY_LEFT_ARROW;
break;
case 63235:
evt.key=evt.KEY_RIGHT_ARROW;
break;
case 63236:
evt.key=evt.KEY_F1;
break;
case 63237:
evt.key=evt.KEY_F2;
break;
case 63238:
evt.key=evt.KEY_F3;
break;
case 63239:
evt.key=evt.KEY_F4;
break;
case 63240:
evt.key=evt.KEY_F5;
break;
case 63241:
evt.key=evt.KEY_F6;
break;
case 63242:
evt.key=evt.KEY_F7;
break;
case 63243:
evt.key=evt.KEY_F8;
break;
case 63244:
evt.key=evt.KEY_F9;
break;
case 63245:
evt.key=evt.KEY_F10;
break;
case 63246:
evt.key=evt.KEY_F11;
break;
case 63247:
evt.key=evt.KEY_F12;
break;
case 63250:
evt.key=evt.KEY_PAUSE;
break;
case 63272:
evt.key=evt.KEY_DELETE;
break;
case 63273:
evt.key=evt.KEY_HOME;
break;
case 63275:
evt.key=evt.KEY_END;
break;
case 63276:
evt.key=evt.KEY_PAGE_UP;
break;
case 63277:
evt.key=evt.KEY_PAGE_DOWN;
break;
case 63302:
evt.key=evt.KEY_INSERT;
break;
case 63248:
case 63249:
case 63289:
break;
default:
evt.key=evt.charCode>=evt.KEY_SPACE?String.fromCharCode(evt.charCode):evt.keyCode;
}
}else{
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
}
}
}
}
}
}
if(dojo.render.html.ie){
if(!evt.target){
evt.target=evt.srcElement;
}
if(!evt.currentTarget){
evt.currentTarget=(_28f?_28f:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _293=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_293.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_293.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this._stopPropagation;
evt.preventDefault=this._preventDefault;
}
return evt;
};
this.stopEvent=function(evt){
if(window.event){
evt.cancelBubble=true;
evt.returnValue=false;
}else{
evt.preventDefault();
evt.stopPropagation();
}
};
};
dojo.provide("dojo.event.*");
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(e){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _296=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_296.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_298,_299){
var node=_298.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_299&&node&&node.tagName&&node.tagName.toLowerCase()!=_299.toLowerCase()){
node=dojo.dom.nextElement(node,_299);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_29b,_29c){
var node=_29b.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_29c&&node&&node.tagName&&node.tagName.toLowerCase()!=_29c.toLowerCase()){
node=dojo.dom.prevElement(node,_29c);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_29f){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_29f&&_29f.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_29f);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_2a1){
if(!node){
return null;
}
if(_2a1){
_2a1=_2a1.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_2a1&&_2a1.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_2a1);
}
return node;
};
dojo.dom.moveChildren=function(_2a2,_2a3,trim){
var _2a5=0;
if(trim){
while(_2a2.hasChildNodes()&&_2a2.firstChild.nodeType==dojo.dom.TEXT_NODE){
_2a2.removeChild(_2a2.firstChild);
}
while(_2a2.hasChildNodes()&&_2a2.lastChild.nodeType==dojo.dom.TEXT_NODE){
_2a2.removeChild(_2a2.lastChild);
}
}
while(_2a2.hasChildNodes()){
_2a3.appendChild(_2a2.firstChild);
_2a5++;
}
return _2a5;
};
dojo.dom.copyChildren=function(_2a6,_2a7,trim){
var _2a9=_2a6.cloneNode(true);
return this.moveChildren(_2a9,_2a7,trim);
};
dojo.dom.replaceChildren=function(node,_2ab){
var _2ac=[];
if(dojo.render.html.ie){
for(var i=0;i<node.childNodes.length;i++){
_2ac.push(node.childNodes[i]);
}
}
dojo.dom.removeChildren(node);
node.appendChild(_2ab);
for(var i=0;i<_2ac.length;i++){
dojo.dom.destroyNode(_2ac[i]);
}
};
dojo.dom.removeChildren=function(node){
var _2af=node.childNodes.length;
while(node.hasChildNodes()){
dojo.dom.removeNode(node.firstChild);
}
return _2af;
};
dojo.dom.replaceNode=function(node,_2b1){
return node.parentNode.replaceChild(_2b1,node);
};
dojo.dom.destroyNode=function(node){
if(node.parentNode){
node=dojo.dom.removeNode(node);
}
if(node.nodeType!=3){
if(dojo.evalObjPath("dojo.event.browser.clean",false)){
dojo.event.browser.clean(node);
}
if(dojo.render.html.ie){
node.outerHTML="";
}
}
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_2b5,_2b6){
var _2b7=[];
var _2b8=(_2b5&&(_2b5 instanceof Function||typeof _2b5=="function"));
while(node){
if(!_2b8||_2b5(node)){
_2b7.push(node);
}
if(_2b6&&_2b7.length>0){
return _2b7[0];
}
node=node.parentNode;
}
if(_2b6){
return null;
}
return _2b7;
};
dojo.dom.getAncestorsByTag=function(node,tag,_2bb){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_2bb);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_2c0,_2c1){
if(_2c1&&node){
node=node.parentNode;
}
while(node){
if(node==_2c0){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _2c4=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _2c5=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_2c5.length;i++){
try{
doc=new ActiveXObject(_2c5[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_2c4.implementation)&&(_2c4.implementation.createDocument)){
doc=_2c4.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_2c8){
if(!_2c8){
_2c8="text/xml";
}
if(!dj_undef("DOMParser")){
var _2c9=new DOMParser();
return _2c9.parseFromString(str,_2c8);
}else{
if(!dj_undef("ActiveXObject")){
var _2ca=dojo.dom.createDocument();
if(_2ca){
_2ca.async=false;
_2ca.loadXML(str);
return _2ca;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _2cb=dojo.doc();
if(_2cb.createElement){
var tmp=_2cb.createElement("xml");
tmp.innerHTML=str;
if(_2cb.implementation&&_2cb.implementation.createDocument){
var _2cd=_2cb.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_2cd.importNode(tmp.childNodes.item(i),true);
}
return _2cd;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_2d0){
if(_2d0.firstChild){
_2d0.insertBefore(node,_2d0.firstChild);
}else{
_2d0.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_2d3){
if((_2d3!=true)&&(node===ref||node.nextSibling===ref)){
return false;
}
var _2d4=ref.parentNode;
_2d4.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_2d7){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_2d7!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_2d7);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_2db){
if((!node)||(!ref)||(!_2db)){
return false;
}
switch(_2db.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_2dd,_2de){
var _2df=_2dd.childNodes;
if(!_2df.length||_2df.length==_2de){
_2dd.appendChild(node);
return true;
}
if(_2de==0){
return dojo.dom.prependChild(node,_2dd);
}
return dojo.dom.insertAfter(node,_2df[_2de-1]);
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _2e2=dojo.doc();
dojo.dom.replaceChildren(node,_2e2.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _2e3="";
if(node==null){
return _2e3;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_2e3+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_2e3+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _2e3;
}
};
dojo.dom.hasParent=function(node){
return Boolean(node&&node.parentNode&&dojo.dom.isNode(node.parentNode));
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_2e9,_2ea,_2eb){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_2e9,_2ea,_2eb);
}else{
var _2ec=elem.ownerDocument;
var _2ed=_2ec.createNode(2,_2ea,_2e9);
_2ed.nodeValue=_2eb;
elem.setAttributeNode(_2ed);
}
};
dojo.provide("dojo.html.common");
dojo.lang.mixin(dojo.html,dojo.dom);
dojo.html.getEventTarget=function(evt){
if(!evt){
evt=dojo.global().event||{};
}
var t=(evt.srcElement?evt.srcElement:(evt.target?evt.target:null));
while((t)&&(t.nodeType!=1)){
t=t.parentNode;
}
return t;
};
dojo.html.getViewport=function(){
var _2f0=dojo.global();
var _2f1=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_2f1.documentElement.clientWidth;
h=_2f0.innerHeight;
}else{
if(!dojo.render.html.opera&&_2f0.innerWidth){
w=_2f0.innerWidth;
h=_2f0.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_2f1,"documentElement.clientWidth")){
var w2=_2f1.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_2f1.documentElement.clientHeight;
}else{
if(dojo.body().clientWidth){
w=dojo.body().clientWidth;
h=dojo.body().clientHeight;
}
}
}
}
return {width:w,height:h};
};
dojo.html.getScroll=function(){
var _2f5=dojo.global();
var _2f6=dojo.doc();
var top=_2f5.pageYOffset||_2f6.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_2f5.pageXOffset||_2f6.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _2fb=dojo.doc();
var _2fc=dojo.byId(node);
type=type.toLowerCase();
while((_2fc)&&(_2fc.nodeName.toLowerCase()!=type)){
if(_2fc==(_2fb["body"]||_2fb["documentElement"])){
return null;
}
_2fc=_2fc.parentNode;
}
return _2fc;
};
dojo.html.getAttribute=function(node,attr){
node=dojo.byId(node);
if((!node)||(!node.getAttribute)){
return null;
}
var ta=typeof attr=="string"?attr:new String(attr);
var v=node.getAttribute(ta.toUpperCase());
if((v)&&(typeof v=="string")&&(v!="")){
return v;
}
if(v&&v.value){
return v.value;
}
if((node.getAttributeNode)&&(node.getAttributeNode(ta))){
return (node.getAttributeNode(ta)).value;
}else{
if(node.getAttribute(ta)){
return node.getAttribute(ta);
}else{
if(node.getAttribute(ta.toLowerCase())){
return node.getAttribute(ta.toLowerCase());
}
}
}
return null;
};
dojo.html.hasAttribute=function(node,attr){
return dojo.html.getAttribute(dojo.byId(node),attr)?true:false;
};
dojo.html.getCursorPosition=function(e){
e=e||dojo.global().event;
var _304={x:0,y:0};
if(e.pageX||e.pageY){
_304.x=e.pageX;
_304.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_304.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_304.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _304;
};
dojo.html.isTag=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName.toLowerCase()==String(arguments[i]).toLowerCase()){
return String(arguments[i]).toLowerCase();
}
}
}
return "";
};
if(dojo.render.html.ie&&!dojo.render.html.ie70){
if(window.location.href.substr(0,6).toLowerCase()!="https:"){
(function(){
var _309=dojo.doc().createElement("script");
_309.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_309);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_30d,uri){
var loc=dojo.hostenv.getModuleSymbols(_30d).join("/");
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri()+loc,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _312=new dojo.uri.Uri(arguments[i].toString());
var _313=new dojo.uri.Uri(uri.toString());
if((_312.path=="")&&(_312.scheme==null)&&(_312.authority==null)&&(_312.query==null)){
if(_312.fragment!=null){
_313.fragment=_312.fragment;
}
_312=_313;
}else{
if(_312.scheme==null){
_312.scheme=_313.scheme;
if(_312.authority==null){
_312.authority=_313.authority;
if(_312.path.charAt(0)!="/"){
var path=_313.path.substring(0,_313.path.lastIndexOf("/")+1)+_312.path;
var segs=path.split("/");
for(var j=0;j<segs.length;j++){
if(segs[j]=="."){
if(j==segs.length-1){
segs[j]="";
}else{
segs.splice(j,1);
j--;
}
}else{
if(j>0&&!(j==1&&segs[0]=="")&&segs[j]==".."&&segs[j-1]!=".."){
if(j==segs.length-1){
segs.splice(j,1);
segs[j-1]="";
}else{
segs.splice(j-1,2);
j-=2;
}
}
}
}
_312.path=segs.join("/");
}
}
}
}
uri="";
if(_312.scheme!=null){
uri+=_312.scheme+":";
}
if(_312.authority!=null){
uri+="//"+_312.authority;
}
uri+=_312.path;
if(_312.query!=null){
uri+="?"+_312.query;
}
if(_312.fragment!=null){
uri+="#"+_312.fragment;
}
}
this.uri=uri.toString();
var _317="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_317));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_317="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_317));
this.user=r[3]||null;
this.password=r[4]||null;
this.host=r[5];
this.port=r[7]||null;
}
this.toString=function(){
return this.uri;
};
};
};
dojo.provide("dojo.html.style");
dojo.html.getClass=function(node){
node=dojo.byId(node);
if(!node){
return "";
}
var cs="";
if(node.className){
cs=node.className;
}else{
if(dojo.html.hasAttribute(node,"class")){
cs=dojo.html.getAttribute(node,"class");
}
}
return cs.replace(/^\s+|\s+$/g,"");
};
dojo.html.getClasses=function(node){
var c=dojo.html.getClass(node);
return (c=="")?[]:c.split(/\s+/g);
};
dojo.html.hasClass=function(node,_31e){
return (new RegExp("(^|\\s+)"+_31e+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_320){
_320+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_320);
};
dojo.html.addClass=function(node,_322){
if(dojo.html.hasClass(node,_322)){
return false;
}
_322=(dojo.html.getClass(node)+" "+_322).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_322);
};
dojo.html.setClass=function(node,_324){
node=dojo.byId(node);
var cs=new String(_324);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_324);
node.className=cs;
}else{
return false;
}
}
}
catch(e){
dojo.debug("dojo.html.setClass() failed",e);
}
return true;
};
dojo.html.removeClass=function(node,_327,_328){
try{
if(!_328){
var _329=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_327+"(\\s+|$)"),"$1$2");
}else{
var _329=dojo.html.getClass(node).replace(_327,"");
}
dojo.html.setClass(node,_329);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_32b,_32c){
dojo.html.removeClass(node,_32c);
dojo.html.addClass(node,_32b);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_32d,_32e,_32f,_330,_331){
_331=false;
var _332=dojo.doc();
_32e=dojo.byId(_32e)||_332;
var _333=_32d.split(/\s+/g);
var _334=[];
if(_330!=1&&_330!=2){
_330=0;
}
var _335=new RegExp("(\\s|^)(("+_333.join(")|(")+"))(\\s|$)");
var _336=_333.join(" ").length;
var _337=[];
if(!_331&&_332.evaluate){
var _338=".//"+(_32f||"*")+"[contains(";
if(_330!=dojo.html.classMatchType.ContainsAny){
_338+="concat(' ',@class,' '), ' "+_333.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_330==2){
_338+=" and string-length(@class)="+_336+"]";
}else{
_338+="]";
}
}else{
_338+="concat(' ',@class,' '), ' "+_333.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _339=_332.evaluate(_338,_32e,null,XPathResult.ANY_TYPE,null);
var _33a=_339.iterateNext();
while(_33a){
try{
_337.push(_33a);
_33a=_339.iterateNext();
}
catch(e){
break;
}
}
return _337;
}else{
if(!_32f){
_32f="*";
}
_337=_32e.getElementsByTagName(_32f);
var node,i=0;
outer:
while(node=_337[i++]){
var _33d=dojo.html.getClasses(node);
if(_33d.length==0){
continue outer;
}
var _33e=0;
for(var j=0;j<_33d.length;j++){
if(_335.test(_33d[j])){
if(_330==dojo.html.classMatchType.ContainsAny){
_334.push(node);
continue outer;
}else{
_33e++;
}
}else{
if(_330==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_33e==_333.length){
if((_330==dojo.html.classMatchType.IsOnly)&&(_33e==_33d.length)){
_334.push(node);
}else{
if(_330==dojo.html.classMatchType.ContainsAll){
_334.push(node);
}
}
}
}
return _334;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_340){
var arr=_340.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_344){
return _344.replace(/([A-Z])/g,"-$1").toLowerCase();
};
dojo.html.getComputedStyle=function(node,_346,_347){
node=dojo.byId(node);
var _346=dojo.html.toSelectorCase(_346);
var _348=dojo.html.toCamelCase(_346);
if(!node||!node.style){
return _347;
}else{
if(document.defaultView&&dojo.html.isDescendantOf(node,node.ownerDocument)){
try{
var cs=document.defaultView.getComputedStyle(node,"");
if(cs){
return cs.getPropertyValue(_346);
}
}
catch(e){
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_346);
}else{
return _347;
}
}
}else{
if(node.currentStyle){
return node.currentStyle[_348];
}
}
}
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_346);
}else{
return _347;
}
};
dojo.html.getStyleProperty=function(node,_34b){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_34b)]:undefined);
};
dojo.html.getStyle=function(node,_34d){
var _34e=dojo.html.getStyleProperty(node,_34d);
return (_34e?_34e:dojo.html.getComputedStyle(node,_34d));
};
dojo.html.setStyle=function(node,_350,_351){
node=dojo.byId(node);
if(node&&node.style){
var _352=dojo.html.toCamelCase(_350);
node.style[_352]=_351;
}
};
dojo.html.setStyleText=function(_353,text){
try{
_353.style.cssText=text;
}
catch(e){
_353.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_355,_356){
if(!_356.style.cssText){
_355.setAttribute("style",_356.getAttribute("style"));
}else{
_355.style.cssText=_356.style.cssText;
}
dojo.html.addClass(_355,dojo.html.getClass(_356));
};
dojo.html.getUnitValue=function(node,_358,_359){
var s=dojo.html.getComputedStyle(node,_358);
if((!s)||((s=="auto")&&(_359))){
return {value:0,units:"px"};
}
var _35b=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_35b){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_35b[1]),units:_35b[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
dojo.html.getPixelValue=function(node,_35d,_35e){
var _35f=dojo.html.getUnitValue(node,_35d,_35e);
if(isNaN(_35f.value)){
return 0;
}
if((_35f.value)&&(_35f.units!="px")){
return NaN;
}
return _35f.value;
};
dojo.html.setPositivePixelValue=function(node,_361,_362){
if(isNaN(_362)){
return false;
}
node.style[_361]=Math.max(0,_362)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_363,_364,_365){
if(!dojo.html.styleSheet){
if(document.createStyleSheet){
dojo.html.styleSheet=document.createStyleSheet();
}else{
if(document.styleSheets[0]){
dojo.html.styleSheet=document.styleSheets[0];
}else{
return null;
}
}
}
if(arguments.length<3){
if(dojo.html.styleSheet.cssRules){
_365=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_365=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_363+" { "+_364+" }";
return dojo.html.styleSheet.insertRule(rule,_365);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_363,_364,_365);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_367){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_367){
_367=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_367);
}
}else{
if(document.styleSheets[0]){
if(!_367){
_367=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_367);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_36a,_36b){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _36c=dojo.hostenv.getText(URI,false,_36b);
if(_36c===null){
return;
}
_36c=dojo.html.fixPathsInCssText(_36c,URI);
if(_36a){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_36c)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _371=doc.getElementsByTagName("style");
for(var i=0;i<_371.length;i++){
if(_371[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _372=dojo.html.insertCssText(_36c,doc);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_36c,"nodeRef":_372});
if(_372&&djConfig.isDebug){
_372.setAttribute("dbgHref",URI);
}
return _372;
};
dojo.html.insertCssText=function(_373,doc,URI){
if(!_373){
return;
}
if(!doc){
doc=document;
}
if(URI){
_373=dojo.html.fixPathsInCssText(_373,URI);
}
var _376=doc.createElement("style");
_376.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_376);
}
if(_376.styleSheet){
var _378=function(){
try{
_376.styleSheet.cssText=_373;
}
catch(e){
dojo.debug(e);
}
};
if(_376.styleSheet.disabled){
setTimeout(_378,10);
}else{
_378();
}
}else{
var _379=doc.createTextNode(_373);
_376.appendChild(_379);
}
return _376;
};
dojo.html.fixPathsInCssText=function(_37a,URI){
if(!_37a||!URI){
return;
}
var _37c,str="",url="",_37f="[\\t\\s\\w\\(\\)\\/\\.\\\\'\"-:#=&?~]+";
var _380=new RegExp("url\\(\\s*("+_37f+")\\s*\\)");
var _381=/(file|https?|ftps?):\/\//;
regexTrim=new RegExp("^[\\s]*(['\"]?)("+_37f+")\\1[\\s]*?$");
if(dojo.render.html.ie55||dojo.render.html.ie60){
var _382=new RegExp("AlphaImageLoader\\((.*)src=['\"]("+_37f+")['\"]");
while(_37c=_382.exec(_37a)){
url=_37c[2].replace(regexTrim,"$2");
if(!_381.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_37a.substring(0,_37c.index)+"AlphaImageLoader("+_37c[1]+"src='"+url+"'";
_37a=_37a.substr(_37c.index+_37c[0].length);
}
_37a=str+_37a;
str="";
}
while(_37c=_380.exec(_37a)){
url=_37c[1].replace(regexTrim,"$2");
if(!_381.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_37a.substring(0,_37c.index)+"url("+url+")";
_37a=_37a.substr(_37c.index+_37c[0].length);
}
return str+_37a;
};
dojo.html.setActiveStyleSheet=function(_383){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_383){
a.disabled=false;
}
}
}
};
dojo.html.getActiveStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")&&!a.disabled){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.getPreferredStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("rel").indexOf("alt")==-1&&a.getAttribute("title")){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.applyBrowserClass=function(node){
var drh=dojo.render.html;
var _38f={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _38f){
if(_38f[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.provide("dojo.html.display");
dojo.html._toggle=function(node,_392,_393){
node=dojo.byId(node);
_393(node,!_392(node));
return _392(node);
};
dojo.html.show=function(node){
node=dojo.byId(node);
if(dojo.html.getStyleProperty(node,"display")=="none"){
dojo.html.setStyle(node,"display",(node.dojoDisplayCache||""));
node.dojoDisplayCache=undefined;
}
};
dojo.html.hide=function(node){
node=dojo.byId(node);
if(typeof node["dojoDisplayCache"]=="undefined"){
var d=dojo.html.getStyleProperty(node,"display");
if(d!="none"){
node.dojoDisplayCache=d;
}
}
dojo.html.setStyle(node,"display","none");
};
dojo.html.setShowing=function(node,_398){
dojo.html[(_398?"show":"hide")](node);
};
dojo.html.isShowing=function(node){
return (dojo.html.getStyleProperty(node,"display")!="none");
};
dojo.html.toggleShowing=function(node){
return dojo.html._toggle(node,dojo.html.isShowing,dojo.html.setShowing);
};
dojo.html.displayMap={tr:"",td:"",th:"",img:"inline",span:"inline",input:"inline",button:"inline"};
dojo.html.suggestDisplayByTagName=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
var tag=node.tagName.toLowerCase();
return (tag in dojo.html.displayMap?dojo.html.displayMap[tag]:"block");
}
};
dojo.html.setDisplay=function(node,_39e){
dojo.html.setStyle(node,"display",((_39e instanceof String||typeof _39e=="string")?_39e:(_39e?dojo.html.suggestDisplayByTagName(node):"none")));
};
dojo.html.isDisplayed=function(node){
return (dojo.html.getComputedStyle(node,"display")!="none");
};
dojo.html.toggleDisplay=function(node){
return dojo.html._toggle(node,dojo.html.isDisplayed,dojo.html.setDisplay);
};
dojo.html.setVisibility=function(node,_3a2){
dojo.html.setStyle(node,"visibility",((_3a2 instanceof String||typeof _3a2=="string")?_3a2:(_3a2?"visible":"hidden")));
};
dojo.html.isVisible=function(node){
return (dojo.html.getComputedStyle(node,"visibility")!="hidden");
};
dojo.html.toggleVisibility=function(node){
return dojo.html._toggle(node,dojo.html.isVisible,dojo.html.setVisibility);
};
dojo.html.setOpacity=function(node,_3a6,_3a7){
node=dojo.byId(node);
var h=dojo.render.html;
if(!_3a7){
if(_3a6>=1){
if(h.ie){
dojo.html.clearOpacity(node);
return;
}else{
_3a6=0.999999;
}
}else{
if(_3a6<0){
_3a6=0;
}
}
}
if(h.ie){
if(node.nodeName.toLowerCase()=="tr"){
var tds=node.getElementsByTagName("td");
for(var x=0;x<tds.length;x++){
tds[x].style.filter="Alpha(Opacity="+_3a6*100+")";
}
}
node.style.filter="Alpha(Opacity="+_3a6*100+")";
}else{
if(h.moz){
node.style.opacity=_3a6;
node.style.MozOpacity=_3a6;
}else{
if(h.safari){
node.style.opacity=_3a6;
node.style.KhtmlOpacity=_3a6;
}else{
node.style.opacity=_3a6;
}
}
}
};
dojo.html.clearOpacity=function(node){
node=dojo.byId(node);
var ns=node.style;
var h=dojo.render.html;
if(h.ie){
try{
if(node.filters&&node.filters.alpha){
ns.filter="";
}
}
catch(e){
}
}else{
if(h.moz){
ns.opacity=1;
ns.MozOpacity=1;
}else{
if(h.safari){
ns.opacity=1;
ns.KhtmlOpacity=1;
}else{
ns.opacity=1;
}
}
}
};
dojo.html.getOpacity=function(node){
node=dojo.byId(node);
var h=dojo.render.html;
if(h.ie){
var opac=(node.filters&&node.filters.alpha&&typeof node.filters.alpha.opacity=="number"?node.filters.alpha.opacity:100)/100;
}else{
var opac=node.style.opacity||node.style.MozOpacity||node.style.KhtmlOpacity||1;
}
return opac>=0.999999?1:Number(opac);
};
dojo.provide("dojo.html.layout");
dojo.html.sumAncestorProperties=function(node,prop){
node=dojo.byId(node);
if(!node){
return 0;
}
var _3b3=0;
while(node){
if(dojo.html.getComputedStyle(node,"position")=="fixed"){
return 0;
}
var val=node[prop];
if(val){
_3b3+=val-0;
if(node==dojo.body()){
break;
}
}
node=node.parentNode;
}
return _3b3;
};
dojo.html.setStyleAttributes=function(node,_3b6){
node=dojo.byId(node);
var _3b7=_3b6.replace(/(;)?\s*$/,"").split(";");
for(var i=0;i<_3b7.length;i++){
var _3b9=_3b7[i].split(":");
var name=_3b9[0].replace(/\s*$/,"").replace(/^\s*/,"").toLowerCase();
var _3bb=_3b9[1].replace(/\s*$/,"").replace(/^\s*/,"");
switch(name){
case "opacity":
dojo.html.setOpacity(node,_3bb);
break;
case "content-height":
dojo.html.setContentBox(node,{height:_3bb});
break;
case "content-width":
dojo.html.setContentBox(node,{width:_3bb});
break;
case "outer-height":
dojo.html.setMarginBox(node,{height:_3bb});
break;
case "outer-width":
dojo.html.setMarginBox(node,{width:_3bb});
break;
default:
node.style[dojo.html.toCamelCase(name)]=_3bb;
}
}
};
dojo.html.boxSizing={MARGIN_BOX:"margin-box",BORDER_BOX:"border-box",PADDING_BOX:"padding-box",CONTENT_BOX:"content-box"};
dojo.html.getAbsolutePosition=dojo.html.abs=function(node,_3bd,_3be){
node=dojo.byId(node);
var _3bf=dojo.doc();
var ret={x:0,y:0};
var bs=dojo.html.boxSizing;
if(!_3be){
_3be=bs.CONTENT_BOX;
}
var _3c2=2;
var _3c3;
switch(_3be){
case bs.MARGIN_BOX:
_3c3=3;
break;
case bs.BORDER_BOX:
_3c3=2;
break;
case bs.PADDING_BOX:
default:
_3c3=1;
break;
case bs.CONTENT_BOX:
_3c3=0;
break;
}
var h=dojo.render.html;
var db=_3bf["body"]||_3bf["documentElement"];
if(h.ie){
with(node.getBoundingClientRect()){
ret.x=left-2;
ret.y=top-2;
}
}else{
if(_3bf["getBoxObjectFor"]){
_3c2=1;
try{
var bo=_3bf.getBoxObjectFor(node);
ret.x=bo.x-dojo.html.sumAncestorProperties(node,"scrollLeft");
ret.y=bo.y-dojo.html.sumAncestorProperties(node,"scrollTop");
}
catch(e){
}
}else{
if(node["offsetParent"]){
var _3c7;
if((h.safari)&&(node.style.getPropertyValue("position")=="absolute")&&(node.parentNode==db)){
_3c7=db;
}else{
_3c7=db.parentNode;
}
if(node.parentNode!=db){
var nd=node;
if(dojo.render.html.opera){
nd=db;
}
ret.x-=dojo.html.sumAncestorProperties(nd,"scrollLeft");
ret.y-=dojo.html.sumAncestorProperties(nd,"scrollTop");
}
var _3c9=node;
do{
var n=_3c9["offsetLeft"];
if(!h.opera||n>0){
ret.x+=isNaN(n)?0:n;
}
var m=_3c9["offsetTop"];
ret.y+=isNaN(m)?0:m;
_3c9=_3c9.offsetParent;
}while((_3c9!=_3c7)&&(_3c9!=null));
}else{
if(node["x"]&&node["y"]){
ret.x+=isNaN(node.x)?0:node.x;
ret.y+=isNaN(node.y)?0:node.y;
}
}
}
}
if(_3bd){
var _3cc=dojo.html.getScroll();
ret.y+=_3cc.top;
ret.x+=_3cc.left;
}
var _3cd=[dojo.html.getPaddingExtent,dojo.html.getBorderExtent,dojo.html.getMarginExtent];
if(_3c2>_3c3){
for(var i=_3c3;i<_3c2;++i){
ret.y+=_3cd[i](node,"top");
ret.x+=_3cd[i](node,"left");
}
}else{
if(_3c2<_3c3){
for(var i=_3c3;i>_3c2;--i){
ret.y-=_3cd[i-1](node,"top");
ret.x-=_3cd[i-1](node,"left");
}
}
}
ret.top=ret.y;
ret.left=ret.x;
return ret;
};
dojo.html.isPositionAbsolute=function(node){
return (dojo.html.getComputedStyle(node,"position")=="absolute");
};
dojo.html._getComponentPixelValues=function(node,_3d1,_3d2,_3d3){
var _3d4=["top","bottom","left","right"];
var obj={};
for(var i in _3d4){
side=_3d4[i];
obj[side]=_3d2(node,_3d1+side,_3d3);
}
obj.width=obj.left+obj.right;
obj.height=obj.top+obj.bottom;
return obj;
};
dojo.html.getMargin=function(node){
return dojo.html._getComponentPixelValues(node,"margin-",dojo.html.getPixelValue,dojo.html.isPositionAbsolute(node));
};
dojo.html.getBorder=function(node){
return dojo.html._getComponentPixelValues(node,"",dojo.html.getBorderExtent);
};
dojo.html.getBorderExtent=function(node,side){
return (dojo.html.getStyle(node,"border-"+side+"-style")=="none"?0:dojo.html.getPixelValue(node,"border-"+side+"-width"));
};
dojo.html.getMarginExtent=function(node,side){
return dojo.html.getPixelValue(node,"margin-"+side,dojo.html.isPositionAbsolute(node));
};
dojo.html.getPaddingExtent=function(node,side){
return dojo.html.getPixelValue(node,"padding-"+side,true);
};
dojo.html.getPadding=function(node){
return dojo.html._getComponentPixelValues(node,"padding-",dojo.html.getPixelValue,true);
};
dojo.html.getPadBorder=function(node){
var pad=dojo.html.getPadding(node);
var _3e2=dojo.html.getBorder(node);
return {width:pad.width+_3e2.width,height:pad.height+_3e2.height};
};
dojo.html.getBoxSizing=function(node){
var h=dojo.render.html;
var bs=dojo.html.boxSizing;
if(((h.ie)||(h.opera))&&node.nodeName!="IMG"){
var cm=document["compatMode"];
if((cm=="BackCompat")||(cm=="QuirksMode")){
return bs.BORDER_BOX;
}else{
return bs.CONTENT_BOX;
}
}else{
if(arguments.length==0){
node=document.documentElement;
}
var _3e7=dojo.html.getStyle(node,"-moz-box-sizing");
if(!_3e7){
_3e7=dojo.html.getStyle(node,"box-sizing");
}
return (_3e7?_3e7:bs.CONTENT_BOX);
}
};
dojo.html.isBorderBox=function(node){
return (dojo.html.getBoxSizing(node)==dojo.html.boxSizing.BORDER_BOX);
};
dojo.html.getBorderBox=function(node){
node=dojo.byId(node);
return {width:node.offsetWidth,height:node.offsetHeight};
};
dojo.html.getPaddingBox=function(node){
var box=dojo.html.getBorderBox(node);
var _3ec=dojo.html.getBorder(node);
return {width:box.width-_3ec.width,height:box.height-_3ec.height};
};
dojo.html.getContentBox=function(node){
node=dojo.byId(node);
var _3ee=dojo.html.getPadBorder(node);
return {width:node.offsetWidth-_3ee.width,height:node.offsetHeight-_3ee.height};
};
dojo.html.setContentBox=function(node,args){
node=dojo.byId(node);
var _3f1=0;
var _3f2=0;
var isbb=dojo.html.isBorderBox(node);
var _3f4=(isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var ret={};
if(typeof args.width!="undefined"){
_3f1=args.width+_3f4.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_3f1);
}
if(typeof args.height!="undefined"){
_3f2=args.height+_3f4.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_3f2);
}
return ret;
};
dojo.html.getMarginBox=function(node){
var _3f7=dojo.html.getBorderBox(node);
var _3f8=dojo.html.getMargin(node);
return {width:_3f7.width+_3f8.width,height:_3f7.height+_3f8.height};
};
dojo.html.setMarginBox=function(node,args){
node=dojo.byId(node);
var _3fb=0;
var _3fc=0;
var isbb=dojo.html.isBorderBox(node);
var _3fe=(!isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var _3ff=dojo.html.getMargin(node);
var ret={};
if(typeof args.width!="undefined"){
_3fb=args.width-_3fe.width;
_3fb-=_3ff.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_3fb);
}
if(typeof args.height!="undefined"){
_3fc=args.height-_3fe.height;
_3fc-=_3ff.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_3fc);
}
return ret;
};
dojo.html.getElementBox=function(node,type){
var bs=dojo.html.boxSizing;
switch(type){
case bs.MARGIN_BOX:
return dojo.html.getMarginBox(node);
case bs.BORDER_BOX:
return dojo.html.getBorderBox(node);
case bs.PADDING_BOX:
return dojo.html.getPaddingBox(node);
case bs.CONTENT_BOX:
default:
return dojo.html.getContentBox(node);
}
};
dojo.html.toCoordinateObject=dojo.html.toCoordinateArray=function(_404,_405,_406){
if(!_404.nodeType&&!(_404 instanceof String||typeof _404=="string")&&("width" in _404||"height" in _404||"left" in _404||"x" in _404||"top" in _404||"y" in _404)){
var ret={left:_404.left||_404.x||0,top:_404.top||_404.y||0,width:_404.width||0,height:_404.height||0};
}else{
var node=dojo.byId(_404);
var pos=dojo.html.abs(node,_405,_406);
var _40a=dojo.html.getMarginBox(node);
var ret={left:pos.left,top:pos.top,width:_40a.width,height:_40a.height};
}
ret.x=ret.left;
ret.y=ret.top;
return ret;
};
dojo.html.setMarginBoxWidth=dojo.html.setOuterWidth=function(node,_40c){
return dojo.html._callDeprecated("setMarginBoxWidth","setMarginBox",arguments,"width");
};
dojo.html.setMarginBoxHeight=dojo.html.setOuterHeight=function(){
return dojo.html._callDeprecated("setMarginBoxHeight","setMarginBox",arguments,"height");
};
dojo.html.getMarginBoxWidth=dojo.html.getOuterWidth=function(){
return dojo.html._callDeprecated("getMarginBoxWidth","getMarginBox",arguments,null,"width");
};
dojo.html.getMarginBoxHeight=dojo.html.getOuterHeight=function(){
return dojo.html._callDeprecated("getMarginBoxHeight","getMarginBox",arguments,null,"height");
};
dojo.html.getTotalOffset=function(node,type,_40f){
return dojo.html._callDeprecated("getTotalOffset","getAbsolutePosition",arguments,null,type);
};
dojo.html.getAbsoluteX=function(node,_411){
return dojo.html._callDeprecated("getAbsoluteX","getAbsolutePosition",arguments,null,"x");
};
dojo.html.getAbsoluteY=function(node,_413){
return dojo.html._callDeprecated("getAbsoluteY","getAbsolutePosition",arguments,null,"y");
};
dojo.html.totalOffsetLeft=function(node,_415){
return dojo.html._callDeprecated("totalOffsetLeft","getAbsolutePosition",arguments,null,"left");
};
dojo.html.totalOffsetTop=function(node,_417){
return dojo.html._callDeprecated("totalOffsetTop","getAbsolutePosition",arguments,null,"top");
};
dojo.html.getMarginWidth=function(node){
return dojo.html._callDeprecated("getMarginWidth","getMargin",arguments,null,"width");
};
dojo.html.getMarginHeight=function(node){
return dojo.html._callDeprecated("getMarginHeight","getMargin",arguments,null,"height");
};
dojo.html.getBorderWidth=function(node){
return dojo.html._callDeprecated("getBorderWidth","getBorder",arguments,null,"width");
};
dojo.html.getBorderHeight=function(node){
return dojo.html._callDeprecated("getBorderHeight","getBorder",arguments,null,"height");
};
dojo.html.getPaddingWidth=function(node){
return dojo.html._callDeprecated("getPaddingWidth","getPadding",arguments,null,"width");
};
dojo.html.getPaddingHeight=function(node){
return dojo.html._callDeprecated("getPaddingHeight","getPadding",arguments,null,"height");
};
dojo.html.getPadBorderWidth=function(node){
return dojo.html._callDeprecated("getPadBorderWidth","getPadBorder",arguments,null,"width");
};
dojo.html.getPadBorderHeight=function(node){
return dojo.html._callDeprecated("getPadBorderHeight","getPadBorder",arguments,null,"height");
};
dojo.html.getBorderBoxWidth=dojo.html.getInnerWidth=function(){
return dojo.html._callDeprecated("getBorderBoxWidth","getBorderBox",arguments,null,"width");
};
dojo.html.getBorderBoxHeight=dojo.html.getInnerHeight=function(){
return dojo.html._callDeprecated("getBorderBoxHeight","getBorderBox",arguments,null,"height");
};
dojo.html.getContentBoxWidth=dojo.html.getContentWidth=function(){
return dojo.html._callDeprecated("getContentBoxWidth","getContentBox",arguments,null,"width");
};
dojo.html.getContentBoxHeight=dojo.html.getContentHeight=function(){
return dojo.html._callDeprecated("getContentBoxHeight","getContentBox",arguments,null,"height");
};
dojo.html.setContentBoxWidth=dojo.html.setContentWidth=function(node,_421){
return dojo.html._callDeprecated("setContentBoxWidth","setContentBox",arguments,"width");
};
dojo.html.setContentBoxHeight=dojo.html.setContentHeight=function(node,_423){
return dojo.html._callDeprecated("setContentBoxHeight","setContentBox",arguments,"height");
};
dojo.provide("dojo.dnd.HtmlDragManager");
dojo.declare("dojo.dnd.HtmlDragManager",dojo.dnd.DragManager,{disabled:false,nestedTargets:false,mouseDownTimer:null,dsCounter:0,dsPrefix:"dojoDragSource",dropTargetDimensions:[],currentDropTarget:null,previousDropTarget:null,_dragTriggered:false,selectedSources:[],dragObjects:[],currentX:null,currentY:null,lastX:null,lastY:null,mouseDownX:null,mouseDownY:null,threshold:7,dropAcceptable:false,cancelEvent:function(e){
e.stopPropagation();
e.preventDefault();
},registerDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _427=dp+"Idx_"+(this.dsCounter++);
ds.dragSourceId=_427;
this.dragSources[_427]=ds;
ds.domNode.setAttribute(dp,_427);
if(dojo.render.html.ie){
dojo.event.browser.addListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},unregisterDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _42a=ds.dragSourceId;
delete ds.dragSourceId;
delete this.dragSources[_42a];
ds.domNode.setAttribute(dp,null);
if(dojo.render.html.ie){
dojo.event.browser.removeListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},registerDropTarget:function(dt){
this.dropTargets.push(dt);
},unregisterDropTarget:function(dt){
var _42d=dojo.lang.find(this.dropTargets,dt,true);
if(_42d>=0){
this.dropTargets.splice(_42d,1);
}
},getDragSource:function(e){
var tn=e.target;
if(tn===dojo.body()){
return;
}
var ta=dojo.html.getAttribute(tn,this.dsPrefix);
while((!ta)&&(tn)){
tn=tn.parentNode;
if((!tn)||(tn===dojo.body())){
return;
}
ta=dojo.html.getAttribute(tn,this.dsPrefix);
}
return this.dragSources[ta];
},onKeyDown:function(e){
},onMouseDown:function(e){
if(this.disabled){
return;
}
if(dojo.render.html.ie){
if(e.button!=1){
return;
}
}else{
if(e.which!=1){
return;
}
}
var _433=e.target.nodeType==dojo.html.TEXT_NODE?e.target.parentNode:e.target;
if(dojo.html.isTag(_433,"button","textarea","input","select","option")){
return;
}
var ds=this.getDragSource(e);
if(!ds){
return;
}
if(!dojo.lang.inArray(this.selectedSources,ds)){
this.selectedSources.push(ds);
ds.onSelected();
}
this.mouseDownX=e.pageX;
this.mouseDownY=e.pageY;
e.preventDefault();
dojo.event.connect(document,"onmousemove",this,"onMouseMove");
},onMouseUp:function(e,_436){
if(this.selectedSources.length==0){
return;
}
this.mouseDownX=null;
this.mouseDownY=null;
this._dragTriggered=false;
e.dragSource=this.dragSource;
if((!e.shiftKey)&&(!e.ctrlKey)){
if(this.currentDropTarget){
this.currentDropTarget.onDropStart();
}
dojo.lang.forEach(this.dragObjects,function(_437){
var ret=null;
if(!_437){
return;
}
if(this.currentDropTarget){
e.dragObject=_437;
var ce=this.currentDropTarget.domNode.childNodes;
if(ce.length>0){
e.dropTarget=ce[0];
while(e.dropTarget==_437.domNode){
e.dropTarget=e.dropTarget.nextSibling;
}
}else{
e.dropTarget=this.currentDropTarget.domNode;
}
if(this.dropAcceptable){
ret=this.currentDropTarget.onDrop(e);
}else{
this.currentDropTarget.onDragOut(e);
}
}
e.dragStatus=this.dropAcceptable&&ret?"dropSuccess":"dropFailure";
dojo.lang.delayThese([function(){
try{
_437.dragSource.onDragEnd(e);
}
catch(err){
var _43a={};
for(var i in e){
if(i=="type"){
_43a.type="mouseup";
continue;
}
_43a[i]=e[i];
}
_437.dragSource.onDragEnd(_43a);
}
},function(){
_437.onDragEnd(e);
}]);
},this);
this.selectedSources=[];
this.dragObjects=[];
this.dragSource=null;
if(this.currentDropTarget){
this.currentDropTarget.onDropEnd();
}
}else{
}
dojo.event.disconnect(document,"onmousemove",this,"onMouseMove");
this.currentDropTarget=null;
},onScroll:function(){
for(var i=0;i<this.dragObjects.length;i++){
if(this.dragObjects[i].updateDragOffset){
this.dragObjects[i].updateDragOffset();
}
}
if(this.dragObjects.length){
this.cacheTargetLocations();
}
},_dragStartDistance:function(x,y){
if((!this.mouseDownX)||(!this.mouseDownX)){
return;
}
var dx=Math.abs(x-this.mouseDownX);
var dx2=dx*dx;
var dy=Math.abs(y-this.mouseDownY);
var dy2=dy*dy;
return parseInt(Math.sqrt(dx2+dy2),10);
},cacheTargetLocations:function(){
dojo.profile.start("cacheTargetLocations");
this.dropTargetDimensions=[];
dojo.lang.forEach(this.dropTargets,function(_443){
var tn=_443.domNode;
if(!tn||!_443.accepts(this.dragSource)){
return;
}
var abs=dojo.html.getAbsolutePosition(tn,true);
var bb=dojo.html.getBorderBox(tn);
this.dropTargetDimensions.push([[abs.x,abs.y],[abs.x+bb.width,abs.y+bb.height],_443]);
},this);
dojo.profile.end("cacheTargetLocations");
},onMouseMove:function(e){
if((dojo.render.html.ie)&&(e.button!=1)){
this.currentDropTarget=null;
this.onMouseUp(e,true);
return;
}
if((this.selectedSources.length)&&(!this.dragObjects.length)){
var dx;
var dy;
if(!this._dragTriggered){
this._dragTriggered=(this._dragStartDistance(e.pageX,e.pageY)>this.threshold);
if(!this._dragTriggered){
return;
}
dx=e.pageX-this.mouseDownX;
dy=e.pageY-this.mouseDownY;
}
this.dragSource=this.selectedSources[0];
dojo.lang.forEach(this.selectedSources,function(_44a){
if(!_44a){
return;
}
var tdo=_44a.onDragStart(e);
if(tdo){
tdo.onDragStart(e);
tdo.dragOffset.y+=dy;
tdo.dragOffset.x+=dx;
tdo.dragSource=_44a;
this.dragObjects.push(tdo);
}
},this);
this.previousDropTarget=null;
this.cacheTargetLocations();
}
dojo.lang.forEach(this.dragObjects,function(_44c){
if(_44c){
_44c.onDragMove(e);
}
});
if(this.currentDropTarget){
var c=dojo.html.toCoordinateObject(this.currentDropTarget.domNode,true);
var dtp=[[c.x,c.y],[c.x+c.width,c.y+c.height]];
}
if((!this.nestedTargets)&&(dtp)&&(this.isInsideBox(e,dtp))){
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}else{
var _44f=this.findBestTarget(e);
if(_44f.target===null){
if(this.currentDropTarget){
this.currentDropTarget.onDragOut(e);
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget=null;
}
this.dropAcceptable=false;
return;
}
if(this.currentDropTarget!==_44f.target){
if(this.currentDropTarget){
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget.onDragOut(e);
}
this.currentDropTarget=_44f.target;
e.dragObjects=this.dragObjects;
this.dropAcceptable=this.currentDropTarget.onDragOver(e);
}else{
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}
}
},findBestTarget:function(e){
var _451=this;
var _452=new Object();
_452.target=null;
_452.points=null;
dojo.lang.every(this.dropTargetDimensions,function(_453){
if(!_451.isInsideBox(e,_453)){
return true;
}
_452.target=_453[2];
_452.points=_453;
return Boolean(_451.nestedTargets);
});
return _452;
},isInsideBox:function(e,_455){
if((e.pageX>_455[0][0])&&(e.pageX<_455[1][0])&&(e.pageY>_455[0][1])&&(e.pageY<_455[1][1])){
return true;
}
return false;
},onMouseOver:function(e){
},onMouseOut:function(e){
}});
dojo.dnd.dragManager=new dojo.dnd.HtmlDragManager();
(function(){
var d=document;
var dm=dojo.dnd.dragManager;
dojo.event.connect(d,"onkeydown",dm,"onKeyDown");
dojo.event.connect(d,"onmouseover",dm,"onMouseOver");
dojo.event.connect(d,"onmouseout",dm,"onMouseOut");
dojo.event.connect(d,"onmousedown",dm,"onMouseDown");
dojo.event.connect(d,"onmouseup",dm,"onMouseUp");
dojo.event.connect(window,"onscroll",dm,"onScroll");
})();
dojo.provide("dojo.html.*");
dojo.provide("dojo.html.util");
dojo.html.getElementWindow=function(_45a){
return dojo.html.getDocumentWindow(_45a.ownerDocument);
};
dojo.html.getDocumentWindow=function(doc){
if(dojo.render.html.safari&&!doc._parentWindow){
var fix=function(win){
win.document._parentWindow=win;
for(var i=0;i<win.frames.length;i++){
fix(win.frames[i]);
}
};
fix(window.top);
}
if(dojo.render.html.ie&&window!==document.parentWindow&&!doc._parentWindow){
doc.parentWindow.execScript("document._parentWindow = window;","Javascript");
var win=doc._parentWindow;
doc._parentWindow=null;
return win;
}
return doc._parentWindow||doc.parentWindow||doc.defaultView;
};
dojo.html.getAbsolutePositionExt=function(node,_461,_462,_463){
var _464=dojo.html.getElementWindow(node);
var ret=dojo.withGlobal(_464,"getAbsolutePosition",dojo.html,arguments);
var win=dojo.html.getElementWindow(node);
if(_463!=win&&win.frameElement){
var ext=dojo.html.getAbsolutePositionExt(win.frameElement,_461,_462,_463);
ret.x+=ext.x;
ret.y+=ext.y;
}
ret.top=ret.y;
ret.left=ret.x;
return ret;
};
dojo.html.gravity=function(node,e){
node=dojo.byId(node);
var _46a=dojo.html.getCursorPosition(e);
with(dojo.html){
var _46b=getAbsolutePosition(node,true);
var bb=getBorderBox(node);
var _46d=_46b.x+(bb.width/2);
var _46e=_46b.y+(bb.height/2);
}
with(dojo.html.gravity){
return ((_46a.x<_46d?WEST:EAST)|(_46a.y<_46e?NORTH:SOUTH));
}
};
dojo.html.gravity.NORTH=1;
dojo.html.gravity.SOUTH=1<<1;
dojo.html.gravity.EAST=1<<2;
dojo.html.gravity.WEST=1<<3;
dojo.html.overElement=function(_46f,e){
_46f=dojo.byId(_46f);
var _471=dojo.html.getCursorPosition(e);
var bb=dojo.html.getBorderBox(_46f);
var _473=dojo.html.getAbsolutePosition(_46f,true,dojo.html.boxSizing.BORDER_BOX);
var top=_473.y;
var _475=top+bb.height;
var left=_473.x;
var _477=left+bb.width;
return (_471.x>=left&&_471.x<=_477&&_471.y>=top&&_471.y<=_475);
};
dojo.html.renderedTextContent=function(node){
node=dojo.byId(node);
var _479="";
if(node==null){
return _479;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
var _47b="unknown";
try{
_47b=dojo.html.getStyle(node.childNodes[i],"display");
}
catch(E){
}
switch(_47b){
case "block":
case "list-item":
case "run-in":
case "table":
case "table-row-group":
case "table-header-group":
case "table-footer-group":
case "table-row":
case "table-column-group":
case "table-column":
case "table-cell":
case "table-caption":
_479+="\n";
_479+=dojo.html.renderedTextContent(node.childNodes[i]);
_479+="\n";
break;
case "none":
break;
default:
if(node.childNodes[i].tagName&&node.childNodes[i].tagName.toLowerCase()=="br"){
_479+="\n";
}else{
_479+=dojo.html.renderedTextContent(node.childNodes[i]);
}
break;
}
break;
case 3:
case 2:
case 4:
var text=node.childNodes[i].nodeValue;
var _47d="unknown";
try{
_47d=dojo.html.getStyle(node,"text-transform");
}
catch(E){
}
switch(_47d){
case "capitalize":
var _47e=text.split(" ");
for(var i=0;i<_47e.length;i++){
_47e[i]=_47e[i].charAt(0).toUpperCase()+_47e[i].substring(1);
}
text=_47e.join(" ");
break;
case "uppercase":
text=text.toUpperCase();
break;
case "lowercase":
text=text.toLowerCase();
break;
default:
break;
}
switch(_47d){
case "nowrap":
break;
case "pre-wrap":
break;
case "pre-line":
break;
case "pre":
break;
default:
text=text.replace(/\s+/," ");
if(/\s$/.test(_479)){
text.replace(/^\s/,"");
}
break;
}
_479+=text;
break;
default:
break;
}
}
return _479;
};
dojo.html.createNodesFromText=function(txt,trim){
if(trim){
txt=txt.replace(/^\s+|\s+$/g,"");
}
var tn=dojo.doc().createElement("div");
tn.style.visibility="hidden";
dojo.body().appendChild(tn);
var _482="none";
if((/^<t[dh][\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody><tr>"+txt+"</tr></tbody></table>";
_482="cell";
}else{
if((/^<tr[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody>"+txt+"</tbody></table>";
_482="row";
}else{
if((/^<(thead|tbody|tfoot)[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table>"+txt+"</table>";
_482="section";
}
}
}
tn.innerHTML=txt;
if(tn["normalize"]){
tn.normalize();
}
var _483=null;
switch(_482){
case "cell":
_483=tn.getElementsByTagName("tr")[0];
break;
case "row":
_483=tn.getElementsByTagName("tbody")[0];
break;
case "section":
_483=tn.getElementsByTagName("table")[0];
break;
default:
_483=tn;
break;
}
var _484=[];
for(var x=0;x<_483.childNodes.length;x++){
_484.push(_483.childNodes[x].cloneNode(true));
}
tn.style.display="none";
dojo.html.destroyNode(tn);
return _484;
};
dojo.html.placeOnScreen=function(node,_487,_488,_489,_48a,_48b,_48c){
if(_487 instanceof Array||typeof _487=="array"){
_48c=_48b;
_48b=_48a;
_48a=_489;
_489=_488;
_488=_487[1];
_487=_487[0];
}
if(_48b instanceof String||typeof _48b=="string"){
_48b=_48b.split(",");
}
if(!isNaN(_489)){
_489=[Number(_489),Number(_489)];
}else{
if(!(_489 instanceof Array||typeof _489=="array")){
_489=[0,0];
}
}
var _48d=dojo.html.getScroll().offset;
var view=dojo.html.getViewport();
node=dojo.byId(node);
var _48f=node.style.display;
node.style.display="";
var bb=dojo.html.getBorderBox(node);
var w=bb.width;
var h=bb.height;
node.style.display=_48f;
if(!(_48b instanceof Array||typeof _48b=="array")){
_48b=["TL"];
}
var _493,_494,_495=Infinity,_496;
for(var _497=0;_497<_48b.length;++_497){
var _498=_48b[_497];
var _499=true;
var tryX=_487-(_498.charAt(1)=="L"?0:w)+_489[0]*(_498.charAt(1)=="L"?1:-1);
var tryY=_488-(_498.charAt(0)=="T"?0:h)+_489[1]*(_498.charAt(0)=="T"?1:-1);
if(_48a){
tryX-=_48d.x;
tryY-=_48d.y;
}
if(tryX<0){
tryX=0;
_499=false;
}
if(tryY<0){
tryY=0;
_499=false;
}
var x=tryX+w;
if(x>view.width){
x=view.width-w;
_499=false;
}else{
x=tryX;
}
x=Math.max(_489[0],x)+_48d.x;
var y=tryY+h;
if(y>view.height){
y=view.height-h;
_499=false;
}else{
y=tryY;
}
y=Math.max(_489[1],y)+_48d.y;
if(_499){
_493=x;
_494=y;
_495=0;
_496=_498;
break;
}else{
var dist=Math.pow(x-tryX-_48d.x,2)+Math.pow(y-tryY-_48d.y,2);
if(_495>dist){
_495=dist;
_493=x;
_494=y;
_496=_498;
}
}
}
if(!_48c){
node.style.left=_493+"px";
node.style.top=_494+"px";
}
return {left:_493,top:_494,x:_493,y:_494,dist:_495,corner:_496};
};
dojo.html.placeOnScreenAroundElement=function(node,_4a0,_4a1,_4a2,_4a3,_4a4){
var best,_4a6=Infinity;
_4a0=dojo.byId(_4a0);
var _4a7=_4a0.style.display;
_4a0.style.display="";
var mb=dojo.html.getElementBox(_4a0,_4a2);
var _4a9=mb.width;
var _4aa=mb.height;
var _4ab=dojo.html.getAbsolutePosition(_4a0,true,_4a2);
_4a0.style.display=_4a7;
for(var _4ac in _4a3){
var pos,_4ae,_4af;
var _4b0=_4a3[_4ac];
_4ae=_4ab.x+(_4ac.charAt(1)=="L"?0:_4a9);
_4af=_4ab.y+(_4ac.charAt(0)=="T"?0:_4aa);
pos=dojo.html.placeOnScreen(node,_4ae,_4af,_4a1,true,_4b0,true);
if(pos.dist==0){
best=pos;
break;
}else{
if(_4a6>pos.dist){
_4a6=pos.dist;
best=pos;
}
}
}
if(!_4a4){
node.style.left=best.left+"px";
node.style.top=best.top+"px";
}
return best;
};
dojo.html.scrollIntoView=function(node){
if(!node){
return;
}
if(dojo.render.html.ie){
if(dojo.html.getBorderBox(node.parentNode).height<=node.parentNode.scrollHeight){
node.scrollIntoView(false);
}
}else{
if(dojo.render.html.mozilla){
node.scrollIntoView(false);
}else{
var _4b2=node.parentNode;
var _4b3=_4b2.scrollTop+dojo.html.getBorderBox(_4b2).height;
var _4b4=node.offsetTop+dojo.html.getMarginBox(node).height;
if(_4b3<_4b4){
_4b2.scrollTop+=(_4b4-_4b3);
}else{
if(_4b2.scrollTop>node.offsetTop){
_4b2.scrollTop-=(_4b2.scrollTop-node.offsetTop);
}
}
}
}
};
dojo.provide("dojo.html.selection");
dojo.html.selectionType={NONE:0,TEXT:1,CONTROL:2};
dojo.html.clearSelection=function(){
var _4b5=dojo.global();
var _4b6=dojo.doc();
try{
if(_4b5["getSelection"]){
if(dojo.render.html.safari){
_4b5.getSelection().collapse();
}else{
_4b5.getSelection().removeAllRanges();
}
}else{
if(_4b6.selection){
if(_4b6.selection.empty){
_4b6.selection.empty();
}else{
if(_4b6.selection.clear){
_4b6.selection.clear();
}
}
}
}
return true;
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.html.disableSelection=function(_4b7){
_4b7=dojo.byId(_4b7)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_4b7.style.MozUserSelect="none";
}else{
if(h.safari){
_4b7.style.KhtmlUserSelect="none";
}else{
if(h.ie){
_4b7.unselectable="on";
}else{
return false;
}
}
}
return true;
};
dojo.html.enableSelection=function(_4b9){
_4b9=dojo.byId(_4b9)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_4b9.style.MozUserSelect="";
}else{
if(h.safari){
_4b9.style.KhtmlUserSelect="";
}else{
if(h.ie){
_4b9.unselectable="off";
}else{
return false;
}
}
}
return true;
};
dojo.html.selectInputText=function(_4bb){
var _4bc=dojo.global();
var _4bd=dojo.doc();
_4bb=dojo.byId(_4bb);
if(_4bd["selection"]&&dojo.body()["createTextRange"]){
var _4be=_4bb.createTextRange();
_4be.moveStart("character",0);
_4be.moveEnd("character",_4bb.value.length);
_4be.select();
}else{
if(_4bc["getSelection"]){
var _4bf=_4bc.getSelection();
_4bb.setSelectionRange(0,_4bb.value.length);
}
}
_4bb.focus();
};
dojo.lang.mixin(dojo.html.selection,{getType:function(){
if(dojo.doc()["selection"]){
return dojo.html.selectionType[dojo.doc().selection.type.toUpperCase()];
}else{
var _4c0=dojo.html.selectionType.TEXT;
var oSel;
try{
oSel=dojo.global().getSelection();
}
catch(e){
}
if(oSel&&oSel.rangeCount==1){
var _4c2=oSel.getRangeAt(0);
if(_4c2.startContainer==_4c2.endContainer&&(_4c2.endOffset-_4c2.startOffset)==1&&_4c2.startContainer.nodeType!=dojo.dom.TEXT_NODE){
_4c0=dojo.html.selectionType.CONTROL;
}
}
return _4c0;
}
},isCollapsed:function(){
var _4c3=dojo.global();
var _4c4=dojo.doc();
if(_4c4["selection"]){
return _4c4.selection.createRange().text=="";
}else{
if(_4c3["getSelection"]){
var _4c5=_4c3.getSelection();
if(dojo.lang.isString(_4c5)){
return _4c5=="";
}else{
return _4c5.isCollapsed||_4c5.toString()=="";
}
}
}
},getSelectedElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
if(dojo.doc()["selection"]){
var _4c6=dojo.doc().selection.createRange();
if(_4c6&&_4c6.item){
return dojo.doc().selection.createRange().item(0);
}
}else{
var _4c7=dojo.global().getSelection();
return _4c7.anchorNode.childNodes[_4c7.anchorOffset];
}
}
},getParentElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
var p=dojo.html.selection.getSelectedElement();
if(p){
return p.parentNode;
}
}else{
if(dojo.doc()["selection"]){
return dojo.doc().selection.createRange().parentElement();
}else{
var _4c9=dojo.global().getSelection();
if(_4c9){
var node=_4c9.anchorNode;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.parentNode;
}
return node;
}
}
}
},getSelectedText:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().text;
}else{
var _4cb=dojo.global().getSelection();
if(_4cb){
return _4cb.toString();
}
}
},getSelectedHtml:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().htmlText;
}else{
var _4cc=dojo.global().getSelection();
if(_4cc&&_4cc.rangeCount){
var frag=_4cc.getRangeAt(0).cloneContents();
var div=document.createElement("div");
div.appendChild(frag);
return div.innerHTML;
}
return null;
}
},hasAncestorElement:function(_4cf){
return (dojo.html.selection.getAncestorElement.apply(this,arguments)!=null);
},getAncestorElement:function(_4d0){
var node=dojo.html.selection.getSelectedElement()||dojo.html.selection.getParentElement();
while(node){
if(dojo.html.selection.isTag(node,arguments).length>0){
return node;
}
node=node.parentNode;
}
return null;
},isTag:function(node,tags){
if(node&&node.tagName){
for(var i=0;i<tags.length;i++){
if(node.tagName.toLowerCase()==String(tags[i]).toLowerCase()){
return String(tags[i]).toLowerCase();
}
}
}
return "";
},selectElement:function(_4d5){
var _4d6=dojo.global();
var _4d7=dojo.doc();
_4d5=dojo.byId(_4d5);
if(_4d7.selection&&dojo.body().createTextRange){
try{
var _4d8=dojo.body().createControlRange();
_4d8.addElement(_4d5);
_4d8.select();
}
catch(e){
dojo.html.selection.selectElementChildren(_4d5);
}
}else{
if(_4d6["getSelection"]){
var _4d9=_4d6.getSelection();
if(_4d9["removeAllRanges"]){
var _4d8=_4d7.createRange();
_4d8.selectNode(_4d5);
_4d9.removeAllRanges();
_4d9.addRange(_4d8);
}
}
}
},selectElementChildren:function(_4da){
var _4db=dojo.global();
var _4dc=dojo.doc();
_4da=dojo.byId(_4da);
if(_4dc.selection&&dojo.body().createTextRange){
var _4dd=dojo.body().createTextRange();
_4dd.moveToElementText(_4da);
_4dd.select();
}else{
if(_4db["getSelection"]){
var _4de=_4db.getSelection();
if(_4de["setBaseAndExtent"]){
_4de.setBaseAndExtent(_4da,0,_4da,_4da.innerText.length-1);
}else{
if(_4de["selectAllChildren"]){
_4de.selectAllChildren(_4da);
}
}
}
}
},getBookmark:function(){
var _4df;
var _4e0=dojo.doc();
if(_4e0["selection"]){
var _4e1=_4e0.selection.createRange();
_4df=_4e1.getBookmark();
}else{
var _4e2;
try{
_4e2=dojo.global().getSelection();
}
catch(e){
}
if(_4e2){
var _4e1=_4e2.getRangeAt(0);
_4df=_4e1.cloneRange();
}else{
dojo.debug("No idea how to store the current selection for this browser!");
}
}
return _4df;
},moveToBookmark:function(_4e3){
var _4e4=dojo.doc();
if(_4e4["selection"]){
var _4e5=_4e4.selection.createRange();
_4e5.moveToBookmark(_4e3);
_4e5.select();
}else{
var _4e6;
try{
_4e6=dojo.global().getSelection();
}
catch(e){
}
if(_4e6&&_4e6["removeAllRanges"]){
_4e6.removeAllRanges();
_4e6.addRange(_4e3);
}else{
dojo.debug("No idea how to restore selection for this browser!");
}
}
},collapse:function(_4e7){
if(dojo.global()["getSelection"]){
var _4e8=dojo.global().getSelection();
if(_4e8.removeAllRanges){
if(_4e7){
_4e8.collapseToStart();
}else{
_4e8.collapseToEnd();
}
}else{
dojo.global().getSelection().collapse(_4e7);
}
}else{
if(dojo.doc().selection){
var _4e9=dojo.doc().selection.createRange();
_4e9.collapse(_4e7);
_4e9.select();
}
}
},remove:function(){
if(dojo.doc().selection){
var _4ea=dojo.doc().selection;
if(_4ea.type.toUpperCase()!="NONE"){
_4ea.clear();
}
return _4ea;
}else{
var _4ea=dojo.global().getSelection();
for(var i=0;i<_4ea.rangeCount;i++){
_4ea.getRangeAt(i).deleteContents();
}
return _4ea;
}
}});
dojo.provide("dojo.html.iframe");
dojo.html.iframeContentWindow=function(_4ec){
var win=dojo.html.getDocumentWindow(dojo.html.iframeContentDocument(_4ec))||dojo.html.iframeContentDocument(_4ec).__parent__||(_4ec.name&&document.frames[_4ec.name])||null;
return win;
};
dojo.html.iframeContentDocument=function(_4ee){
var doc=_4ee.contentDocument||((_4ee.contentWindow)&&(_4ee.contentWindow.document))||((_4ee.name)&&(document.frames[_4ee.name])&&(document.frames[_4ee.name].document))||null;
return doc;
};
dojo.html.BackgroundIframe=function(node){
if(dojo.render.html.ie55||dojo.render.html.ie60){
var html="<iframe src='javascript:false'"+" style='position: absolute; left: 0px; top: 0px; width: 100%; height: 100%;"+"z-index: -1; filter:Alpha(Opacity=\"0\");' "+">";
this.iframe=dojo.doc().createElement(html);
this.iframe.tabIndex=-1;
if(node){
node.appendChild(this.iframe);
this.domNode=node;
}else{
dojo.body().appendChild(this.iframe);
this.iframe.style.display="none";
}
}
};
dojo.lang.extend(dojo.html.BackgroundIframe,{iframe:null,onResized:function(){
if(this.iframe&&this.domNode&&this.domNode.parentNode){
var _4f2=dojo.html.getMarginBox(this.domNode);
if(_4f2.width==0||_4f2.height==0){
dojo.lang.setTimeout(this,this.onResized,100);
return;
}
this.iframe.style.width=_4f2.width+"px";
this.iframe.style.height=_4f2.height+"px";
}
},size:function(node){
if(!this.iframe){
return;
}
var _4f4=dojo.html.toCoordinateObject(node,true,dojo.html.boxSizing.BORDER_BOX);
with(this.iframe.style){
width=_4f4.width+"px";
height=_4f4.height+"px";
left=_4f4.left+"px";
top=_4f4.top+"px";
}
},setZIndex:function(node){
if(!this.iframe){
return;
}
if(dojo.dom.isNode(node)){
this.iframe.style.zIndex=dojo.html.getStyle(node,"z-index")-1;
}else{
if(!isNaN(node)){
this.iframe.style.zIndex=node;
}
}
},show:function(){
if(this.iframe){
this.iframe.style.display="block";
}
},hide:function(){
if(this.iframe){
this.iframe.style.display="none";
}
},remove:function(){
if(this.iframe){
dojo.html.removeNode(this.iframe,true);
delete this.iframe;
this.iframe=null;
}
}});
dojo.provide("dojo.gfx.color");
dojo.gfx.color.Color=function(r,g,b,a){
if(dojo.lang.isArray(r)){
this.r=r[0];
this.g=r[1];
this.b=r[2];
this.a=r[3]||1;
}else{
if(dojo.lang.isString(r)){
var rgb=dojo.gfx.color.extractRGB(r);
this.r=rgb[0];
this.g=rgb[1];
this.b=rgb[2];
this.a=g||1;
}else{
if(r instanceof dojo.gfx.color.Color){
this.r=r.r;
this.b=r.b;
this.g=r.g;
this.a=r.a;
}else{
this.r=r;
this.g=g;
this.b=b;
this.a=a;
}
}
}
};
dojo.gfx.color.Color.fromArray=function(arr){
return new dojo.gfx.color.Color(arr[0],arr[1],arr[2],arr[3]);
};
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_4fc){
if(_4fc){
return this.toRgba();
}else{
return [this.r,this.g,this.b];
}
},toRgba:function(){
return [this.r,this.g,this.b,this.a];
},toHex:function(){
return dojo.gfx.color.rgb2hex(this.toRgb());
},toCss:function(){
return "rgb("+this.toRgb().join()+")";
},toString:function(){
return this.toHex();
},blend:function(_4fd,_4fe){
var rgb=null;
if(dojo.lang.isArray(_4fd)){
rgb=_4fd;
}else{
if(_4fd instanceof dojo.gfx.color.Color){
rgb=_4fd.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_4fd).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_4fe);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_502){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_502);
}
if(!_502){
_502=0;
}
_502=Math.min(Math.max(-1,_502),1);
_502=((_502+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_502));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_507){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_507));
};
dojo.gfx.color.extractRGB=function(_508){
var hex="0123456789abcdef";
_508=_508.toLowerCase();
if(_508.indexOf("rgb")==0){
var _50a=_508.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_50a.splice(1,3);
return ret;
}else{
var _50c=dojo.gfx.color.hex2rgb(_508);
if(_50c){
return _50c;
}else{
return dojo.gfx.color.named[_508]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _50e="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_50e+"]","g"),"")!=""){
return null;
}
if(hex.length==3){
rgb[0]=hex.charAt(0)+hex.charAt(0);
rgb[1]=hex.charAt(1)+hex.charAt(1);
rgb[2]=hex.charAt(2)+hex.charAt(2);
}else{
rgb[0]=hex.substring(0,2);
rgb[1]=hex.substring(2,4);
rgb[2]=hex.substring(4);
}
for(var i=0;i<rgb.length;i++){
rgb[i]=_50e.indexOf(rgb[i].charAt(0))*16+_50e.indexOf(rgb[i].charAt(1));
}
return rgb;
};
dojo.gfx.color.rgb2hex=function(r,g,b){
if(dojo.lang.isArray(r)){
g=r[1]||0;
b=r[2]||0;
r=r[0]||0;
}
var ret=dojo.lang.map([r,g,b],function(x){
x=new Number(x);
var s=x.toString(16);
while(s.length<2){
s="0"+s;
}
return s;
});
ret.unshift("#");
return ret.join("");
};
dojo.provide("dojo.lfx.Animation");
dojo.lfx.Line=function(_517,end){
this.start=_517;
this.end=end;
if(dojo.lang.isArray(_517)){
var diff=[];
dojo.lang.forEach(this.start,function(s,i){
diff[i]=this.end[i]-s;
},this);
this.getValue=function(n){
var res=[];
dojo.lang.forEach(this.start,function(s,i){
res[i]=(diff[i]*n)+s;
},this);
return res;
};
}else{
var diff=end-_517;
this.getValue=function(n){
return (diff*n)+this.start;
};
}
};
dojo.lfx.easeDefault=function(n){
if(dojo.render.html.khtml){
return (parseFloat("0.5")+((Math.sin((n+parseFloat("1.5"))*Math.PI))/2));
}else{
return (0.5+((Math.sin((n+1.5)*Math.PI))/2));
}
};
dojo.lfx.easeIn=function(n){
return Math.pow(n,3);
};
dojo.lfx.easeOut=function(n){
return (1-Math.pow(1-n,3));
};
dojo.lfx.easeInOut=function(n){
return ((3*Math.pow(n,2))-(2*Math.pow(n,3)));
};
dojo.lfx.IAnimation=function(){
};
dojo.lang.extend(dojo.lfx.IAnimation,{curve:null,duration:1000,easing:null,repeatCount:0,rate:25,handler:null,beforeBegin:null,onBegin:null,onAnimate:null,onEnd:null,onPlay:null,onPause:null,onStop:null,play:null,pause:null,stop:null,connect:function(evt,_526,_527){
if(!_527){
_527=_526;
_526=this;
}
_527=dojo.lang.hitch(_526,_527);
var _528=this[evt]||function(){
};
this[evt]=function(){
var ret=_528.apply(this,arguments);
_527.apply(this,arguments);
return ret;
};
return this;
},fire:function(evt,args){
if(this[evt]){
this[evt].apply(this,(args||[]));
}
return this;
},repeat:function(_52c){
this.repeatCount=_52c;
return this;
},_active:false,_paused:false});
dojo.lfx.Animation=function(_52d,_52e,_52f,_530,_531,rate){
dojo.lfx.IAnimation.call(this);
if(dojo.lang.isNumber(_52d)||(!_52d&&_52e.getValue)){
rate=_531;
_531=_530;
_530=_52f;
_52f=_52e;
_52e=_52d;
_52d=null;
}else{
if(_52d.getValue||dojo.lang.isArray(_52d)){
rate=_530;
_531=_52f;
_530=_52e;
_52f=_52d;
_52e=null;
_52d=null;
}
}
if(dojo.lang.isArray(_52f)){
this.curve=new dojo.lfx.Line(_52f[0],_52f[1]);
}else{
this.curve=_52f;
}
if(_52e!=null&&_52e>0){
this.duration=_52e;
}
if(_531){
this.repeatCount=_531;
}
if(rate){
this.rate=rate;
}
if(_52d){
dojo.lang.forEach(["handler","beforeBegin","onBegin","onEnd","onPlay","onStop","onAnimate"],function(item){
if(_52d[item]){
this.connect(item,_52d[item]);
}
},this);
}
if(_530&&dojo.lang.isFunction(_530)){
this.easing=_530;
}
};
dojo.inherits(dojo.lfx.Animation,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Animation,{_startTime:null,_endTime:null,_timer:null,_percent:0,_startRepeatCount:0,play:function(_534,_535){
if(_535){
clearTimeout(this._timer);
this._active=false;
this._paused=false;
this._percent=0;
}else{
if(this._active&&!this._paused){
return this;
}
}
this.fire("handler",["beforeBegin"]);
this.fire("beforeBegin");
if(_534>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_535);
}),_534);
return this;
}
this._startTime=new Date().valueOf();
if(this._paused){
this._startTime-=(this.duration*this._percent/100);
}
this._endTime=this._startTime+this.duration;
this._active=true;
this._paused=false;
var step=this._percent/100;
var _537=this.curve.getValue(step);
if(this._percent==0){
if(!this._startRepeatCount){
this._startRepeatCount=this.repeatCount;
}
this.fire("handler",["begin",_537]);
this.fire("onBegin",[_537]);
}
this.fire("handler",["play",_537]);
this.fire("onPlay",[_537]);
this._cycle();
return this;
},pause:function(){
clearTimeout(this._timer);
if(!this._active){
return this;
}
this._paused=true;
var _538=this.curve.getValue(this._percent/100);
this.fire("handler",["pause",_538]);
this.fire("onPause",[_538]);
return this;
},gotoPercent:function(pct,_53a){
clearTimeout(this._timer);
this._active=true;
this._paused=true;
this._percent=pct;
if(_53a){
this.play();
}
return this;
},stop:function(_53b){
clearTimeout(this._timer);
var step=this._percent/100;
if(_53b){
step=1;
}
var _53d=this.curve.getValue(step);
this.fire("handler",["stop",_53d]);
this.fire("onStop",[_53d]);
this._active=false;
this._paused=false;
return this;
},status:function(){
if(this._active){
return this._paused?"paused":"playing";
}else{
return "stopped";
}
return this;
},_cycle:function(){
clearTimeout(this._timer);
if(this._active){
var curr=new Date().valueOf();
var step=(curr-this._startTime)/(this._endTime-this._startTime);
if(step>=1){
step=1;
this._percent=100;
}else{
this._percent=step*100;
}
if((this.easing)&&(dojo.lang.isFunction(this.easing))){
step=this.easing(step);
}
var _540=this.curve.getValue(step);
this.fire("handler",["animate",_540]);
this.fire("onAnimate",[_540]);
if(step<1){
this._timer=setTimeout(dojo.lang.hitch(this,"_cycle"),this.rate);
}else{
this._active=false;
this.fire("handler",["end"]);
this.fire("onEnd");
if(this.repeatCount>0){
this.repeatCount--;
this.play(null,true);
}else{
if(this.repeatCount==-1){
this.play(null,true);
}else{
if(this._startRepeatCount){
this.repeatCount=this._startRepeatCount;
this._startRepeatCount=0;
}
}
}
}
}
return this;
}});
dojo.lfx.Combine=function(_541){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._animsEnded=0;
var _542=arguments;
if(_542.length==1&&(dojo.lang.isArray(_542[0])||dojo.lang.isArrayLike(_542[0]))){
_542=_542[0];
}
dojo.lang.forEach(_542,function(anim){
this._anims.push(anim);
anim.connect("onEnd",dojo.lang.hitch(this,"_onAnimsEnded"));
},this);
};
dojo.inherits(dojo.lfx.Combine,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Combine,{_animsEnded:0,play:function(_544,_545){
if(!this._anims.length){
return this;
}
this.fire("beforeBegin");
if(_544>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_545);
}),_544);
return this;
}
if(_545||this._anims[0].percent==0){
this.fire("onBegin");
}
this.fire("onPlay");
this._animsCall("play",null,_545);
return this;
},pause:function(){
this.fire("onPause");
this._animsCall("pause");
return this;
},stop:function(_546){
this.fire("onStop");
this._animsCall("stop",_546);
return this;
},_onAnimsEnded:function(){
this._animsEnded++;
if(this._animsEnded>=this._anims.length){
this.fire("onEnd");
}
return this;
},_animsCall:function(_547){
var args=[];
if(arguments.length>1){
for(var i=1;i<arguments.length;i++){
args.push(arguments[i]);
}
}
var _54a=this;
dojo.lang.forEach(this._anims,function(anim){
anim[_547](args);
},_54a);
return this;
}});
dojo.lfx.Chain=function(_54c){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._currAnim=-1;
var _54d=arguments;
if(_54d.length==1&&(dojo.lang.isArray(_54d[0])||dojo.lang.isArrayLike(_54d[0]))){
_54d=_54d[0];
}
var _54e=this;
dojo.lang.forEach(_54d,function(anim,i,_551){
this._anims.push(anim);
if(i<_551.length-1){
anim.connect("onEnd",dojo.lang.hitch(this,"_playNext"));
}else{
anim.connect("onEnd",dojo.lang.hitch(this,function(){
this.fire("onEnd");
}));
}
},this);
};
dojo.inherits(dojo.lfx.Chain,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Chain,{_currAnim:-1,play:function(_552,_553){
if(!this._anims.length){
return this;
}
if(_553||!this._anims[this._currAnim]){
this._currAnim=0;
}
var _554=this._anims[this._currAnim];
this.fire("beforeBegin");
if(_552>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_553);
}),_552);
return this;
}
if(_554){
if(this._currAnim==0){
this.fire("handler",["begin",this._currAnim]);
this.fire("onBegin",[this._currAnim]);
}
this.fire("onPlay",[this._currAnim]);
_554.play(null,_553);
}
return this;
},pause:function(){
if(this._anims[this._currAnim]){
this._anims[this._currAnim].pause();
this.fire("onPause",[this._currAnim]);
}
return this;
},playPause:function(){
if(this._anims.length==0){
return this;
}
if(this._currAnim==-1){
this._currAnim=0;
}
var _555=this._anims[this._currAnim];
if(_555){
if(!_555._active||_555._paused){
this.play();
}else{
this.pause();
}
}
return this;
},stop:function(){
var _556=this._anims[this._currAnim];
if(_556){
_556.stop();
this.fire("onStop",[this._currAnim]);
}
return _556;
},_playNext:function(){
if(this._currAnim==-1||this._anims.length==0){
return this;
}
this._currAnim++;
if(this._anims[this._currAnim]){
this._anims[this._currAnim].play(null,true);
}
return this;
}});
dojo.lfx.combine=function(_557){
var _558=arguments;
if(dojo.lang.isArray(arguments[0])){
_558=arguments[0];
}
if(_558.length==1){
return _558[0];
}
return new dojo.lfx.Combine(_558);
};
dojo.lfx.chain=function(_559){
var _55a=arguments;
if(dojo.lang.isArray(arguments[0])){
_55a=arguments[0];
}
if(_55a.length==1){
return _55a[0];
}
return new dojo.lfx.Chain(_55a);
};
dojo.provide("dojo.html.color");
dojo.html.getBackgroundColor=function(node){
node=dojo.byId(node);
var _55c;
do{
_55c=dojo.html.getStyle(node,"background-color");
if(_55c.toLowerCase()=="rgba(0, 0, 0, 0)"){
_55c="transparent";
}
if(node==document.getElementsByTagName("body")[0]){
node=null;
break;
}
node=node.parentNode;
}while(node&&dojo.lang.inArray(["transparent",""],_55c));
if(_55c=="transparent"){
_55c=[255,255,255,0];
}else{
_55c=dojo.gfx.color.extractRGB(_55c);
}
return _55c;
};
dojo.provide("dojo.lfx.html");
dojo.lfx.html._byId=function(_55d){
if(!_55d){
return [];
}
if(dojo.lang.isArrayLike(_55d)){
if(!_55d.alreadyChecked){
var n=[];
dojo.lang.forEach(_55d,function(node){
n.push(dojo.byId(node));
});
n.alreadyChecked=true;
return n;
}else{
return _55d;
}
}else{
var n=[];
n.push(dojo.byId(_55d));
n.alreadyChecked=true;
return n;
}
};
dojo.lfx.html.propertyAnimation=function(_560,_561,_562,_563,_564){
_560=dojo.lfx.html._byId(_560);
var _565={"propertyMap":_561,"nodes":_560,"duration":_562,"easing":_563||dojo.lfx.easeDefault};
var _566=function(args){
if(args.nodes.length==1){
var pm=args.propertyMap;
if(!dojo.lang.isArray(args.propertyMap)){
var parr=[];
for(var _56a in pm){
pm[_56a].property=_56a;
parr.push(pm[_56a]);
}
pm=args.propertyMap=parr;
}
dojo.lang.forEach(pm,function(prop){
if(dj_undef("start",prop)){
if(prop.property!="opacity"){
prop.start=parseInt(dojo.html.getComputedStyle(args.nodes[0],prop.property));
}else{
prop.start=dojo.html.getOpacity(args.nodes[0]);
}
}
});
}
};
var _56c=function(_56d){
var _56e=[];
dojo.lang.forEach(_56d,function(c){
_56e.push(Math.round(c));
});
return _56e;
};
var _570=function(n,_572){
n=dojo.byId(n);
if(!n||!n.style){
return;
}
for(var s in _572){
try{
if(s=="opacity"){
dojo.html.setOpacity(n,_572[s]);
}else{
n.style[s]=_572[s];
}
}
catch(e){
dojo.debug(e);
}
}
};
var _574=function(_575){
this._properties=_575;
this.diffs=new Array(_575.length);
dojo.lang.forEach(_575,function(prop,i){
if(dojo.lang.isFunction(prop.start)){
prop.start=prop.start(prop,i);
}
if(dojo.lang.isFunction(prop.end)){
prop.end=prop.end(prop,i);
}
if(dojo.lang.isArray(prop.start)){
this.diffs[i]=null;
}else{
if(prop.start instanceof dojo.gfx.color.Color){
prop.startRgb=prop.start.toRgb();
prop.endRgb=prop.end.toRgb();
}else{
this.diffs[i]=prop.end-prop.start;
}
}
},this);
this.getValue=function(n){
var ret={};
dojo.lang.forEach(this._properties,function(prop,i){
var _57c=null;
if(dojo.lang.isArray(prop.start)){
}else{
if(prop.start instanceof dojo.gfx.color.Color){
_57c=(prop.units||"rgb")+"(";
for(var j=0;j<prop.startRgb.length;j++){
_57c+=Math.round(((prop.endRgb[j]-prop.startRgb[j])*n)+prop.startRgb[j])+(j<prop.startRgb.length-1?",":"");
}
_57c+=")";
}else{
_57c=((this.diffs[i])*n)+prop.start+(prop.property!="opacity"?prop.units||"px":"");
}
}
ret[dojo.html.toCamelCase(prop.property)]=_57c;
},this);
return ret;
};
};
var anim=new dojo.lfx.Animation({beforeBegin:function(){
_566(_565);
anim.curve=new _574(_565.propertyMap);
},onAnimate:function(_57f){
dojo.lang.forEach(_565.nodes,function(node){
_570(node,_57f);
});
}},_565.duration,null,_565.easing);
if(_564){
for(var x in _564){
if(dojo.lang.isFunction(_564[x])){
anim.connect(x,anim,_564[x]);
}
}
}
return anim;
};
dojo.lfx.html._makeFadeable=function(_582){
var _583=function(node){
if(dojo.render.html.ie){
if((node.style.zoom.length==0)&&(dojo.html.getStyle(node,"zoom")=="normal")){
node.style.zoom="1";
}
if((node.style.width.length==0)&&(dojo.html.getStyle(node,"width")=="auto")){
node.style.width="auto";
}
}
};
if(dojo.lang.isArrayLike(_582)){
dojo.lang.forEach(_582,_583);
}else{
_583(_582);
}
};
dojo.lfx.html.fade=function(_585,_586,_587,_588,_589){
_585=dojo.lfx.html._byId(_585);
var _58a={property:"opacity"};
if(!dj_undef("start",_586)){
_58a.start=_586.start;
}else{
_58a.start=function(){
return dojo.html.getOpacity(_585[0]);
};
}
if(!dj_undef("end",_586)){
_58a.end=_586.end;
}else{
dojo.raise("dojo.lfx.html.fade needs an end value");
}
var anim=dojo.lfx.propertyAnimation(_585,[_58a],_587,_588);
anim.connect("beforeBegin",function(){
dojo.lfx.html._makeFadeable(_585);
});
if(_589){
anim.connect("onEnd",function(){
_589(_585,anim);
});
}
return anim;
};
dojo.lfx.html.fadeIn=function(_58c,_58d,_58e,_58f){
return dojo.lfx.html.fade(_58c,{end:1},_58d,_58e,_58f);
};
dojo.lfx.html.fadeOut=function(_590,_591,_592,_593){
return dojo.lfx.html.fade(_590,{end:0},_591,_592,_593);
};
dojo.lfx.html.fadeShow=function(_594,_595,_596,_597){
_594=dojo.lfx.html._byId(_594);
dojo.lang.forEach(_594,function(node){
dojo.html.setOpacity(node,0);
});
var anim=dojo.lfx.html.fadeIn(_594,_595,_596,_597);
anim.connect("beforeBegin",function(){
if(dojo.lang.isArrayLike(_594)){
dojo.lang.forEach(_594,dojo.html.show);
}else{
dojo.html.show(_594);
}
});
return anim;
};
dojo.lfx.html.fadeHide=function(_59a,_59b,_59c,_59d){
var anim=dojo.lfx.html.fadeOut(_59a,_59b,_59c,function(){
if(dojo.lang.isArrayLike(_59a)){
dojo.lang.forEach(_59a,dojo.html.hide);
}else{
dojo.html.hide(_59a);
}
if(_59d){
_59d(_59a,anim);
}
});
return anim;
};
dojo.lfx.html.wipeIn=function(_59f,_5a0,_5a1,_5a2){
_59f=dojo.lfx.html._byId(_59f);
var _5a3=[];
dojo.lang.forEach(_59f,function(node){
var _5a5={};
var _5a6,_5a7,_5a8;
with(node.style){
_5a6=top;
_5a7=left;
_5a8=position;
top="-9999px";
left="-9999px";
position="absolute";
display="";
}
var _5a9=dojo.html.getBorderBox(node).height;
with(node.style){
top=_5a6;
left=_5a7;
position=_5a8;
display="none";
}
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:1,end:function(){
return _5a9;
}}},_5a0,_5a1);
anim.connect("beforeBegin",function(){
_5a5.overflow=node.style.overflow;
_5a5.height=node.style.height;
with(node.style){
overflow="hidden";
_5a9="1px";
}
dojo.html.show(node);
});
anim.connect("onEnd",function(){
with(node.style){
overflow=_5a5.overflow;
_5a9=_5a5.height;
}
if(_5a2){
_5a2(node,anim);
}
});
_5a3.push(anim);
});
return dojo.lfx.combine(_5a3);
};
dojo.lfx.html.wipeOut=function(_5ab,_5ac,_5ad,_5ae){
_5ab=dojo.lfx.html._byId(_5ab);
var _5af=[];
dojo.lang.forEach(_5ab,function(node){
var _5b1={};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:function(){
return dojo.html.getContentBox(node).height;
},end:1}},_5ac,_5ad,{"beforeBegin":function(){
_5b1.overflow=node.style.overflow;
_5b1.height=node.style.height;
with(node.style){
overflow="hidden";
}
dojo.html.show(node);
},"onEnd":function(){
dojo.html.hide(node);
with(node.style){
overflow=_5b1.overflow;
height=_5b1.height;
}
if(_5ae){
_5ae(node,anim);
}
}});
_5af.push(anim);
});
return dojo.lfx.combine(_5af);
};
dojo.lfx.html.slideTo=function(_5b3,_5b4,_5b5,_5b6,_5b7){
_5b3=dojo.lfx.html._byId(_5b3);
var _5b8=[];
var _5b9=dojo.html.getComputedStyle;
dojo.lang.forEach(_5b3,function(node){
var top=null;
var left=null;
var init=(function(){
var _5be=node;
return function(){
var pos=_5b9(_5be,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_5b9(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_5b9(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_5be,true);
dojo.html.setStyleAttributes(_5be,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:(_5b4.top||0)},"left":{start:left,end:(_5b4.left||0)}},_5b5,_5b6,{"beforeBegin":init});
if(_5b7){
anim.connect("onEnd",function(){
_5b7(_5b3,anim);
});
}
_5b8.push(anim);
});
return dojo.lfx.combine(_5b8);
};
dojo.lfx.html.slideBy=function(_5c2,_5c3,_5c4,_5c5,_5c6){
_5c2=dojo.lfx.html._byId(_5c2);
var _5c7=[];
var _5c8=dojo.html.getComputedStyle;
dojo.lang.forEach(_5c2,function(node){
var top=null;
var left=null;
var init=(function(){
var _5cd=node;
return function(){
var pos=_5c8(_5cd,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_5c8(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_5c8(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_5cd,true);
dojo.html.setStyleAttributes(_5cd,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:top+(_5c3.top||0)},"left":{start:left,end:left+(_5c3.left||0)}},_5c4,_5c5).connect("beforeBegin",init);
if(_5c6){
anim.connect("onEnd",function(){
_5c6(_5c2,anim);
});
}
_5c7.push(anim);
});
return dojo.lfx.combine(_5c7);
};
dojo.lfx.html.explode=function(_5d1,_5d2,_5d3,_5d4,_5d5){
var h=dojo.html;
_5d1=dojo.byId(_5d1);
_5d2=dojo.byId(_5d2);
var _5d7=h.toCoordinateObject(_5d1,true);
var _5d8=document.createElement("div");
h.copyStyle(_5d8,_5d2);
if(_5d2.explodeClassName){
_5d8.className=_5d2.explodeClassName;
}
with(_5d8.style){
position="absolute";
display="none";
var _5d9=h.getStyle(_5d1,"background-color");
backgroundColor=_5d9?_5d9.toLowerCase():"transparent";
backgroundColor=(backgroundColor=="transparent")?"rgb(221, 221, 221)":backgroundColor;
}
dojo.body().appendChild(_5d8);
with(_5d2.style){
visibility="hidden";
display="block";
}
var _5da=h.toCoordinateObject(_5d2,true);
with(_5d2.style){
display="none";
visibility="visible";
}
var _5db={opacity:{start:0.5,end:1}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_5db[type]={start:_5d7[type],end:_5da[type]};
});
var anim=new dojo.lfx.propertyAnimation(_5d8,_5db,_5d3,_5d4,{"beforeBegin":function(){
h.setDisplay(_5d8,"block");
},"onEnd":function(){
h.setDisplay(_5d2,"block");
_5d8.parentNode.removeChild(_5d8);
}});
if(_5d5){
anim.connect("onEnd",function(){
_5d5(_5d2,anim);
});
}
return anim;
};
dojo.lfx.html.implode=function(_5de,end,_5e0,_5e1,_5e2){
var h=dojo.html;
_5de=dojo.byId(_5de);
end=dojo.byId(end);
var _5e4=dojo.html.toCoordinateObject(_5de,true);
var _5e5=dojo.html.toCoordinateObject(end,true);
var _5e6=document.createElement("div");
dojo.html.copyStyle(_5e6,_5de);
if(_5de.explodeClassName){
_5e6.className=_5de.explodeClassName;
}
dojo.html.setOpacity(_5e6,0.3);
with(_5e6.style){
position="absolute";
display="none";
backgroundColor=h.getStyle(_5de,"background-color").toLowerCase();
}
dojo.body().appendChild(_5e6);
var _5e7={opacity:{start:1,end:0.5}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_5e7[type]={start:_5e4[type],end:_5e5[type]};
});
var anim=new dojo.lfx.propertyAnimation(_5e6,_5e7,_5e0,_5e1,{"beforeBegin":function(){
dojo.html.hide(_5de);
dojo.html.show(_5e6);
},"onEnd":function(){
_5e6.parentNode.removeChild(_5e6);
}});
if(_5e2){
anim.connect("onEnd",function(){
_5e2(_5de,anim);
});
}
return anim;
};
dojo.lfx.html.highlight=function(_5ea,_5eb,_5ec,_5ed,_5ee){
_5ea=dojo.lfx.html._byId(_5ea);
var _5ef=[];
dojo.lang.forEach(_5ea,function(node){
var _5f1=dojo.html.getBackgroundColor(node);
var bg=dojo.html.getStyle(node,"background-color").toLowerCase();
var _5f3=dojo.html.getStyle(node,"background-image");
var _5f4=(bg=="transparent"||bg=="rgba(0, 0, 0, 0)");
while(_5f1.length>3){
_5f1.pop();
}
var rgb=new dojo.gfx.color.Color(_5eb);
var _5f6=new dojo.gfx.color.Color(_5f1);
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:rgb,end:_5f6}},_5ec,_5ed,{"beforeBegin":function(){
if(_5f3){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+rgb.toRgb().join(",")+")";
},"onEnd":function(){
if(_5f3){
node.style.backgroundImage=_5f3;
}
if(_5f4){
node.style.backgroundColor="transparent";
}
if(_5ee){
_5ee(node,anim);
}
}});
_5ef.push(anim);
});
return dojo.lfx.combine(_5ef);
};
dojo.lfx.html.unhighlight=function(_5f8,_5f9,_5fa,_5fb,_5fc){
_5f8=dojo.lfx.html._byId(_5f8);
var _5fd=[];
dojo.lang.forEach(_5f8,function(node){
var _5ff=new dojo.gfx.color.Color(dojo.html.getBackgroundColor(node));
var rgb=new dojo.gfx.color.Color(_5f9);
var _601=dojo.html.getStyle(node,"background-image");
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:_5ff,end:rgb}},_5fa,_5fb,{"beforeBegin":function(){
if(_601){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+_5ff.toRgb().join(",")+")";
},"onEnd":function(){
if(_5fc){
_5fc(node,anim);
}
}});
_5fd.push(anim);
});
return dojo.lfx.combine(_5fd);
};
dojo.lang.mixin(dojo.lfx,dojo.lfx.html);
dojo.provide("dojo.lfx.*");
dojo.provide("dojo.dnd.HtmlDragAndDrop");
dojo.declare("dojo.dnd.HtmlDragSource",dojo.dnd.DragSource,{dragClass:"",onDragStart:function(){
var _603=new dojo.dnd.HtmlDragObject(this.dragObject,this.type);
if(this.dragClass){
_603.dragClass=this.dragClass;
}
if(this.constrainToContainer){
_603.constrainTo(this.constrainingContainer||this.domNode.parentNode);
}
return _603;
},setDragHandle:function(node){
node=dojo.byId(node);
dojo.dnd.dragManager.unregisterDragSource(this);
this.domNode=node;
dojo.dnd.dragManager.registerDragSource(this);
},setDragTarget:function(node){
this.dragObject=node;
},constrainTo:function(_606){
this.constrainToContainer=true;
if(_606){
this.constrainingContainer=_606;
}
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragSource(this.dragObjects[i]));
}
},addDragObjects:function(el){
for(var i=0;i<arguments.length;i++){
this.dragObjects.push(arguments[i]);
}
}},function(node,type){
node=dojo.byId(node);
this.dragObjects=[];
this.constrainToContainer=false;
if(node){
this.domNode=node;
this.dragObject=node;
this.type=(type)||(this.domNode.nodeName.toLowerCase());
this.reregister();
}
});
dojo.declare("dojo.dnd.HtmlDragObject",dojo.dnd.DragObject,{dragClass:"",opacity:0.5,createIframe:true,disableX:false,disableY:false,createDragNode:function(){
var node=this.domNode.cloneNode(true);
if(this.dragClass){
dojo.html.addClass(node,this.dragClass);
}
if(this.opacity<1){
dojo.html.setOpacity(node,this.opacity);
}
var ltn=node.tagName.toLowerCase();
var isTr=(ltn=="tr");
if((isTr)||(ltn=="tbody")){
var doc=this.domNode.ownerDocument;
var _610=doc.createElement("table");
if(isTr){
var _611=doc.createElement("tbody");
_610.appendChild(_611);
_611.appendChild(node);
}else{
_610.appendChild(node);
}
var _612=((isTr)?this.domNode:this.domNode.firstChild);
var _613=((isTr)?node:node.firstChild);
var _614=tdp.childNodes;
var _615=_613.childNodes;
for(var i=0;i<_614.length;i++){
if((_615[i])&&(_615[i].style)){
_615[i].style.width=dojo.html.getContentBox(_614[i]).width+"px";
}
}
node=_610;
}
if((dojo.render.html.ie55||dojo.render.html.ie60)&&this.createIframe){
with(node.style){
top="0px";
left="0px";
}
var _617=document.createElement("div");
_617.appendChild(node);
this.bgIframe=new dojo.html.BackgroundIframe(_617);
_617.appendChild(this.bgIframe.iframe);
node=_617;
}
node.style.zIndex=999;
return node;
},onDragStart:function(e){
dojo.html.clearSelection();
this.scrollOffset=dojo.html.getScroll().offset;
this.dragStartPosition=dojo.html.getAbsolutePosition(this.domNode,true);
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.dragClone=this.createDragNode();
this.containingBlockPosition=this.domNode.offsetParent?dojo.html.getAbsolutePosition(this.domNode.offsetParent,true):{x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
with(this.dragClone.style){
position="absolute";
top=this.dragOffset.y+e.pageY+"px";
left=this.dragOffset.x+e.pageX+"px";
}
dojo.body().appendChild(this.dragClone);
dojo.event.topic.publish("dragStart",{source:this});
},getConstraints:function(){
if(this.constrainingContainer.nodeName.toLowerCase()=="body"){
var _619=dojo.html.getViewport();
var _61a=_619.width;
var _61b=_619.height;
var _61c=dojo.html.getScroll().offset;
var x=_61c.x;
var y=_61c.y;
}else{
var _61f=dojo.html.getContentBox(this.constrainingContainer);
_61a=_61f.width;
_61b=_61f.height;
x=this.containingBlockPosition.x+dojo.html.getPixelValue(this.constrainingContainer,"padding-left",true)+dojo.html.getBorderExtent(this.constrainingContainer,"left");
y=this.containingBlockPosition.y+dojo.html.getPixelValue(this.constrainingContainer,"padding-top",true)+dojo.html.getBorderExtent(this.constrainingContainer,"top");
}
var mb=dojo.html.getMarginBox(this.domNode);
return {minX:x,minY:y,maxX:x+_61a-mb.width,maxY:y+_61b-mb.height};
},updateDragOffset:function(){
var _621=dojo.html.getScroll().offset;
if(_621.y!=this.scrollOffset.y){
var diff=_621.y-this.scrollOffset.y;
this.dragOffset.y+=diff;
this.scrollOffset.y=_621.y;
}
if(_621.x!=this.scrollOffset.x){
var diff=_621.x-this.scrollOffset.x;
this.dragOffset.x+=diff;
this.scrollOffset.x=_621.x;
}
},onDragMove:function(e){
this.updateDragOffset();
var x=this.dragOffset.x+e.pageX;
var y=this.dragOffset.y+e.pageY;
if(this.constrainToContainer){
if(x<this.constraints.minX){
x=this.constraints.minX;
}
if(y<this.constraints.minY){
y=this.constraints.minY;
}
if(x>this.constraints.maxX){
x=this.constraints.maxX;
}
if(y>this.constraints.maxY){
y=this.constraints.maxY;
}
}
this.setAbsolutePosition(x,y);
dojo.event.topic.publish("dragMove",{source:this});
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.dragClone.style.top=y+"px";
}
if(!this.disableX){
this.dragClone.style.left=x+"px";
}
},onDragEnd:function(e){
switch(e.dragStatus){
case "dropSuccess":
dojo.html.removeNode(this.dragClone);
this.dragClone=null;
break;
case "dropFailure":
var _629=dojo.html.getAbsolutePosition(this.dragClone,true);
var _62a={left:this.dragStartPosition.x+1,top:this.dragStartPosition.y+1};
var anim=dojo.lfx.slideTo(this.dragClone,_62a,300);
var _62c=this;
dojo.event.connect(anim,"onEnd",function(e){
dojo.html.removeNode(_62c.dragClone);
_62c.dragClone=null;
});
anim.play();
break;
}
dojo.event.topic.publish("dragEnd",{source:this});
},constrainTo:function(_62e){
this.constrainToContainer=true;
if(_62e){
this.constrainingContainer=_62e;
}else{
this.constrainingContainer=this.domNode.parentNode;
}
}},function(node,type){
this.domNode=dojo.byId(node);
this.type=type;
this.constrainToContainer=false;
this.dragSource=null;
this.register();
});
dojo.declare("dojo.dnd.HtmlDropTarget",dojo.dnd.DropTarget,{vertical:false,onDragOver:function(e){
if(!this.accepts(e.dragObjects)){
return false;
}
this.childBoxes=[];
for(var i=0,_633;i<this.domNode.childNodes.length;i++){
_633=this.domNode.childNodes[i];
if(_633.nodeType!=dojo.html.ELEMENT_NODE){
continue;
}
var pos=dojo.html.getAbsolutePosition(_633,true);
var _635=dojo.html.getBorderBox(_633);
this.childBoxes.push({top:pos.y,bottom:pos.y+_635.height,left:pos.x,right:pos.x+_635.width,height:_635.height,width:_635.width,node:_633});
}
return true;
},_getNodeUnderMouse:function(e){
for(var i=0,_638;i<this.childBoxes.length;i++){
with(this.childBoxes[i]){
if(e.pageX>=left&&e.pageX<=right&&e.pageY>=top&&e.pageY<=bottom){
return i;
}
}
}
return -1;
},createDropIndicator:function(){
this.dropIndicator=document.createElement("div");
with(this.dropIndicator.style){
position="absolute";
zIndex=999;
if(this.vertical){
borderLeftWidth="1px";
borderLeftColor="black";
borderLeftStyle="solid";
height=dojo.html.getBorderBox(this.domNode).height+"px";
top=dojo.html.getAbsolutePosition(this.domNode,true).y+"px";
}else{
borderTopWidth="1px";
borderTopColor="black";
borderTopStyle="solid";
width=dojo.html.getBorderBox(this.domNode).width+"px";
left=dojo.html.getAbsolutePosition(this.domNode,true).x+"px";
}
}
},onDragMove:function(e,_63a){
var i=this._getNodeUnderMouse(e);
if(!this.dropIndicator){
this.createDropIndicator();
}
var _63c=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
var hide=false;
if(i<0){
if(this.childBoxes.length){
var _63e=(dojo.html.gravity(this.childBoxes[0].node,e)&_63c);
if(_63e){
hide=true;
}
}else{
var _63e=true;
}
}else{
var _63f=this.childBoxes[i];
var _63e=(dojo.html.gravity(_63f.node,e)&_63c);
if(_63f.node===_63a[0].dragSource.domNode){
hide=true;
}else{
var _640=_63e?(i>0?this.childBoxes[i-1]:_63f):(i<this.childBoxes.length-1?this.childBoxes[i+1]:_63f);
if(_640.node===_63a[0].dragSource.domNode){
hide=true;
}
}
}
if(hide){
this.dropIndicator.style.display="none";
return;
}else{
this.dropIndicator.style.display="";
}
this.placeIndicator(e,_63a,i,_63e);
if(!dojo.html.hasParent(this.dropIndicator)){
dojo.body().appendChild(this.dropIndicator);
}
},placeIndicator:function(e,_642,_643,_644){
var _645=this.vertical?"left":"top";
var _646;
if(_643<0){
if(this.childBoxes.length){
_646=_644?this.childBoxes[0]:this.childBoxes[this.childBoxes.length-1];
}else{
this.dropIndicator.style[_645]=dojo.html.getAbsolutePosition(this.domNode,true)[this.vertical?"x":"y"]+"px";
}
}else{
_646=this.childBoxes[_643];
}
if(_646){
this.dropIndicator.style[_645]=(_644?_646[_645]:_646[this.vertical?"right":"bottom"])+"px";
if(this.vertical){
this.dropIndicator.style.height=_646.height+"px";
this.dropIndicator.style.top=_646.top+"px";
}else{
this.dropIndicator.style.width=_646.width+"px";
this.dropIndicator.style.left=_646.left+"px";
}
}
},onDragOut:function(e){
if(this.dropIndicator){
dojo.html.removeNode(this.dropIndicator);
delete this.dropIndicator;
}
},onDrop:function(e){
this.onDragOut(e);
var i=this._getNodeUnderMouse(e);
var _64a=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
if(i<0){
if(this.childBoxes.length){
if(dojo.html.gravity(this.childBoxes[0].node,e)&_64a){
return this.insert(e,this.childBoxes[0].node,"before");
}else{
return this.insert(e,this.childBoxes[this.childBoxes.length-1].node,"after");
}
}
return this.insert(e,this.domNode,"append");
}
var _64b=this.childBoxes[i];
if(dojo.html.gravity(_64b.node,e)&_64a){
return this.insert(e,_64b.node,"before");
}else{
return this.insert(e,_64b.node,"after");
}
},insert:function(e,_64d,_64e){
var node=e.dragObject.domNode;
if(_64e=="before"){
return dojo.html.insertBefore(node,_64d);
}else{
if(_64e=="after"){
return dojo.html.insertAfter(node,_64d);
}else{
if(_64e=="append"){
_64d.appendChild(node);
return true;
}
}
}
return false;
}},function(node,_651){
if(arguments.length==0){
return;
}
this.domNode=dojo.byId(node);
dojo.dnd.DropTarget.call(this);
if(_651&&dojo.lang.isString(_651)){
_651=[_651];
}
this.acceptedTypes=_651||[];
dojo.dnd.dragManager.registerDropTarget(this);
});
dojo.provide("dojo.dnd.*");
dojo.provide("dojo.dnd.HtmlDragMove");
dojo.declare("dojo.dnd.HtmlDragMoveSource",dojo.dnd.HtmlDragSource,{onDragStart:function(){
var _652=new dojo.dnd.HtmlDragMoveObject(this.dragObject,this.type);
if(this.constrainToContainer){
_652.constrainTo(this.constrainingContainer);
}
return _652;
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragMoveSource(this.dragObjects[i]));
}
}});
dojo.declare("dojo.dnd.HtmlDragMoveObject",dojo.dnd.HtmlDragObject,{onDragStart:function(e){
dojo.html.clearSelection();
this.dragClone=this.domNode;
if(dojo.html.getComputedStyle(this.domNode,"position")!="absolute"){
this.domNode.style.position="relative";
}
var left=parseInt(dojo.html.getComputedStyle(this.domNode,"left"));
var top=parseInt(dojo.html.getComputedStyle(this.domNode,"top"));
this.dragStartPosition={x:isNaN(left)?0:left,y:isNaN(top)?0:top};
this.scrollOffset=dojo.html.getScroll().offset;
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.containingBlockPosition={x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
dojo.event.connect(this.domNode,"onclick",this,"_squelchOnClick");
},onDragEnd:function(e){
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.domNode.style.top=y+"px";
}
if(!this.disableX){
this.domNode.style.left=x+"px";
}
},_squelchOnClick:function(e){
dojo.event.browser.stopEvent(e);
dojo.event.disconnect(this.domNode,"onclick",this,"_squelchOnClick");
}});
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_661,_662){
var out="";
for(var i=0;i<_661;i++){
out+=str;
if(_662&&i<_661-1){
out+=_662;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.string");
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_671,_672,_673){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_671){
this.mimetype=_671;
}
if(_672){
this.transport=_672;
}
if(arguments.length>=4){
this.changeUrl=_673;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_676,_677){
},error:function(type,_679,_67a,_67b){
},timeout:function(type,_67d,_67e,_67f){
},handle:function(type,data,_682,_683){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_684){
if(_684["url"]){
_684.url=_684.url.toString();
}
if(_684["formNode"]){
_684.formNode=dojo.byId(_684.formNode);
}
if(!_684["method"]&&_684["formNode"]&&_684["formNode"].method){
_684.method=_684["formNode"].method;
}
if(!_684["handle"]&&_684["handler"]){
_684.handle=_684.handler;
}
if(!_684["load"]&&_684["loaded"]){
_684.load=_684.loaded;
}
if(!_684["changeUrl"]&&_684["changeURL"]){
_684.changeUrl=_684.changeURL;
}
_684.encoding=dojo.lang.firstValued(_684["encoding"],djConfig["bindEncoding"],"");
_684.sendTransport=dojo.lang.firstValued(_684["sendTransport"],djConfig["ioSendTransport"],false);
var _685=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_684[fn]&&_685(_684[fn])){
continue;
}
if(_684["handle"]&&_685(_684["handle"])){
_684[fn]=_684.handle;
}
}
dojo.lang.mixin(this,_684);
}});
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_68c){
if(!(_68c instanceof dojo.io.Request)){
try{
_68c=new dojo.io.Request(_68c);
}
catch(e){
dojo.debug(e);
}
}
var _68d="";
if(_68c["transport"]){
_68d=_68c["transport"];
if(!this[_68d]){
dojo.io.sendBindError(_68c,"No dojo.io.bind() transport with name '"+_68c["transport"]+"'.");
return _68c;
}
if(!this[_68d].canHandle(_68c)){
dojo.io.sendBindError(_68c,"dojo.io.bind() transport with name '"+_68c["transport"]+"' cannot handle this type of request.");
return _68c;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_68c))){
_68d=tmp;
break;
}
}
if(_68d==""){
dojo.io.sendBindError(_68c,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _68c;
}
}
this[_68d].bind(_68c);
_68c.bindSuccess=true;
return _68c;
};
dojo.io.sendBindError=function(_690,_691){
if((typeof _690.error=="function"||typeof _690.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _692=new dojo.io.Error(_691);
setTimeout(function(){
_690[(typeof _690.error=="function")?"error":"handle"]("error",_692,null,_690);
},50);
}else{
dojo.raise(_691);
}
};
dojo.io.queueBind=function(_693){
if(!(_693 instanceof dojo.io.Request)){
try{
_693=new dojo.io.Request(_693);
}
catch(e){
dojo.debug(e);
}
}
var _694=_693.load;
_693.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_694.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _696=_693.error;
_693.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_696.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_693);
dojo.io._dispatchNextQueueBind();
return _693;
};
dojo.io._dispatchNextQueueBind=function(){
if(!dojo.io._queueBindInFlight){
dojo.io._queueBindInFlight=true;
if(dojo.io._bindQueue.length>0){
dojo.io.bind(dojo.io._bindQueue.shift());
}else{
dojo.io._queueBindInFlight=false;
}
}
};
dojo.io._bindQueue=[];
dojo.io._queueBindInFlight=false;
dojo.io.argsFromMap=function(map,_699,last){
var enc=/utf/i.test(_699||"")?encodeURIComponent:dojo.string.encodeAscii;
var _69c=[];
var _69d=new Object();
for(var name in map){
var _69f=function(elt){
var val=enc(name)+"="+enc(elt);
_69c[(last==name)?"push":"unshift"](val);
};
if(!_69d[name]){
var _6a2=map[name];
if(dojo.lang.isArray(_6a2)){
dojo.lang.forEach(_6a2,_69f);
}else{
_69f(_6a2);
}
}
}
return _69c.join("&");
};
dojo.io.setIFrameSrc=function(_6a3,src,_6a5){
try{
var r=dojo.render.html;
if(!_6a5){
if(r.safari){
_6a3.location=src;
}else{
frames[_6a3.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_6a3.contentWindow.document;
}else{
if(r.safari){
idoc=_6a3.document;
}else{
idoc=_6a3.contentWindow;
}
}
if(!idoc){
_6a3.location=src;
return;
}else{
idoc.location.replace(src);
}
}
}
catch(e){
dojo.debug(e);
dojo.debug("setIFrameSrc: "+e);
}
};
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_6a8){
this.pairs=[];
this.returnWrappers=_6a8||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_6aa,wrap,_6ac,_6ad){
var type=(_6ad)?"unshift":"push";
this.pairs[type]([name,_6aa,wrap,_6ac]);
},match:function(){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[1].apply(this,arguments)){
if((pair[3])||(this.returnWrappers)){
return pair[2];
}else{
return pair[2].apply(this,arguments);
}
}
}
throw new Error("No match found");
},unregister:function(name){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[0]==name){
this.pairs.splice(i,1);
return true;
}
}
return false;
}});
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_6b4,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _6b4.replace(/\%\{(\w+)\}/g,function(_6b7,key){
if(typeof (map[key])!="undefined"&&map[key]!=null){
return map[key];
}
dojo.raise("Substitution not found: "+key);
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
if(arguments.length==0){
str=this;
}
var _6ba=str.split(" ");
for(var i=0;i<_6ba.length;i++){
_6ba[i]=_6ba[i].charAt(0).toUpperCase()+_6ba[i].substring(1);
}
return _6ba.join(" ");
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _6bf=escape(str);
var _6c0,re=/%u([0-9A-F]{4})/i;
while((_6c0=_6bf.match(re))){
var num=Number("0x"+_6c0[1]);
var _6c3=escape("&#"+num+";");
ret+=_6bf.substring(0,_6c0.index)+_6c3;
_6bf=_6bf.substring(_6c0.index+_6c0[0].length);
}
ret+=_6bf.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_6c8){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_6c8){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str){
return str.replace(/\\/gm,"\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm,"\\$1");
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_6d1){
if(_6d1){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_6d5,_6d6){
if(_6d6){
str=str.toLowerCase();
_6d5=_6d5.toLowerCase();
}
return str.indexOf(_6d5)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_6dc){
if(_6dc=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_6dc=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_6de){
var _6df=[];
for(var i=0,_6e1=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_6de){
_6df.push(str.substring(_6e1,i));
_6e1=i+1;
}
}
_6df.push(str.substr(_6e1));
return _6df;
};
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_6e3,wrap,_6e5){
dojo.json.jsonRegistry.register(name,_6e3,wrap,_6e5);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _6e8=typeof (o);
if(_6e8=="undefined"){
return "undefined";
}else{
if((_6e8=="number")||(_6e8=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_6e8=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _6ea;
if(typeof (o.__json__)=="function"){
_6ea=o.__json__();
if(o!==_6ea){
return me(_6ea);
}
}
if(typeof (o.json)=="function"){
_6ea=o.json();
if(o!==_6ea){
return me(_6ea);
}
}
if(_6e8!="function"&&typeof (o.length)=="number"){
var res=[];
for(var i=0;i<o.length;i++){
var val=me(o[i]);
if(typeof (val)!="string"){
val="undefined";
}
res.push(val);
}
return "["+res.join(",")+"]";
}
try{
window.o=o;
_6ea=dojo.json.jsonRegistry.match(o);
return me(_6ea);
}
catch(e){
}
if(_6e8=="function"){
return null;
}
res=[];
for(var k in o){
var _6ef;
if(typeof (k)=="number"){
_6ef="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_6ef=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_6ef+":"+val);
}
return "{"+res.join(",")+"}";
}};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:(!dj_undef("window"))?window.location.href:"",initialHash:(!dj_undef("window"))?window.location.hash:"",moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
dojo.body().appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if(args["changeUrl"]){
hash="#"+((args["changeUrl"]!==true)?args["changeUrl"]:(new Date()).getTime());
if(this.historyStack.length==0&&this.initialState.urlHash==hash){
this.initialState=this._createState(url,args,hash);
return;
}else{
if(this.historyStack.length>0&&this.historyStack[this.historyStack.length-1].urlHash==hash){
this.historyStack[this.historyStack.length-1]=this._createState(url,args,hash);
return;
}
}
this.changingUrl=true;
setTimeout("window.location.href = '"+hash+"'; dojo.undo.browser.changingUrl = false;",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
url=this._loadIframeHistory();
var _6f4=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_6f6){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_6f4.apply(this,[_6f6]);
};
if(args["back"]){
args.back=tcb;
}else{
if(args["backButton"]){
args.backButton=tcb;
}else{
if(args["handle"]){
args.handle=tcb;
}
}
}
var _6f7=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_6f9){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_6f7){
_6f7.apply(this,[_6f9]);
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}else{
if(args["handle"]){
args.handle=tfw;
}
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.undo.browser.checkLocation();",200);
}
}
}
}else{
url=this._loadIframeHistory();
}
this.historyStack.push(this._createState(url,args,hash));
},checkLocation:function(){
if(!this.changingUrl){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash||window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
}
},iframeLoaded:function(evt,_6fc){
if(!dojo.render.html.opera){
var _6fd=this._getUrlQuery(_6fc.href);
if(_6fd==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_6fd==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_6fd==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _6fe=this.historyStack.pop();
if(!_6fe){
return;
}
var last=this.historyStack[this.historyStack.length-1];
if(!last&&this.historyStack.length==0){
last=this.initialState;
}
if(last){
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(_6fe);
},handleForwardButton:function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.forward();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
},_createState:function(url,args,hash){
return {"url":url,"kwArgs":args,"urlHash":hash};
},_getUrlQuery:function(url){
var _705=url.split("?");
if(_705.length<2){
return null;
}else{
return _705[1];
}
},_loadIframeHistory:function(){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
if(!dj_undef("window")){
dojo.io.checkChildrenForFile=function(node){
var _708=false;
var _709=node.getElementsByTagName("input");
dojo.lang.forEach(_709,function(_70a){
if(_708){
return;
}
if(_70a.getAttribute("type")=="file"){
_708=true;
}
});
return _708;
};
dojo.io.formHasFile=function(_70b){
return dojo.io.checkChildrenForFile(_70b);
};
dojo.io.updateNode=function(node,_70d){
node=dojo.byId(node);
var args=_70d;
if(dojo.lang.isString(_70d)){
args={url:_70d};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
dojo.dom.destroyNode(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_714,_715,_716){
if((!_714)||(!_714.tagName)||(!_714.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_716){
_716=dojo.io.formFilter;
}
var enc=/utf/i.test(_715||"")?encodeURIComponent:dojo.string.encodeAscii;
var _718=[];
for(var i=0;i<_714.elements.length;i++){
var elm=_714.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_716(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_718.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_718.push(name+"="+enc(elm.value));
}
}else{
_718.push(name+"="+enc(elm.value));
}
}
}
var _71e=_714.getElementsByTagName("input");
for(var i=0;i<_71e.length;i++){
var _71f=_71e[i];
if(_71f.type.toLowerCase()=="image"&&_71f.form==_714&&_716(_71f)){
var name=enc(_71f.name);
_718.push(name+"="+enc(_71f.value));
_718.push(name+".x=0");
_718.push(name+".y=0");
}
}
return _718.join("&")+"&";
};
dojo.io.FormBind=function(args){
this.bindArgs={};
if(args&&args.formNode){
this.init(args);
}else{
if(args){
this.init({formNode:args});
}
}
};
dojo.lang.extend(dojo.io.FormBind,{form:null,bindArgs:null,clickedButton:null,init:function(args){
var form=dojo.byId(args.formNode);
if(!form||!form.tagName||form.tagName.toLowerCase()!="form"){
throw new Error("FormBind: Couldn't apply, invalid form");
}else{
if(this.form==form){
return;
}else{
if(this.form){
throw new Error("FormBind: Already applied to a form");
}
}
}
dojo.lang.mixin(this.bindArgs,args);
this.form=form;
this.connect(form,"onsubmit","submit");
for(var i=0;i<form.elements.length;i++){
var node=form.elements[i];
if(node&&node.type&&dojo.lang.inArray(["submit","button"],node.type.toLowerCase())){
this.connect(node,"onclick","click");
}
}
var _725=form.getElementsByTagName("input");
for(var i=0;i<_725.length;i++){
var _726=_725[i];
if(_726.type.toLowerCase()=="image"&&_726.form==form){
this.connect(_726,"onclick","click");
}
}
},onSubmit:function(form){
return true;
},submit:function(e){
e.preventDefault();
if(this.onSubmit(this.form)){
dojo.io.bind(dojo.lang.mixin(this.bindArgs,{formFilter:dojo.lang.hitch(this,"formFilter")}));
}
},click:function(e){
var node=e.currentTarget;
if(node.disabled){
return;
}
this.clickedButton=node;
},formFilter:function(node){
var type=(node.type||"").toLowerCase();
var _72d=false;
if(node.disabled||!node.name){
_72d=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_72d=node==this.clickedButton;
}else{
_72d=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _72d;
},connect:function(_72e,_72f,_730){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_72e,_72f,this,_730);
}else{
var fcn=dojo.lang.hitch(this,_730);
_72e[_72f]=function(e){
if(!e){
e=window.event;
}
if(!e.currentTarget){
e.currentTarget=e.srcElement;
}
if(!e.preventDefault){
e.preventDefault=function(){
window.event.returnValue=false;
};
}
fcn(e);
};
}
}});
dojo.io.XMLHTTPTransport=new function(){
var _733=this;
var _734={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_736,_737){
return url+"|"+_736+"|"+_737.toLowerCase();
}
function addToCache(url,_739,_73a,http){
_734[getCacheKey(url,_739,_73a)]=http;
}
function getFromCache(url,_73d,_73e){
return _734[getCacheKey(url,_73d,_73e)];
}
this.clearCache=function(){
_734={};
};
function doLoad(_73f,http,url,_742,_743){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_73f.method.toLowerCase()=="head"){
var _745=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _745;
};
var _746=_745.split(/[\r\n]+/g);
for(var i=0;i<_746.length;i++){
var pair=_746[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_73f.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_73f.mimetype=="text/json"||_73f.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_73f.mimetype=="application/xml")||(_73f.mimetype=="text/xml")){
ret=http.responseXML;
if(!ret||typeof ret=="string"||!http.getResponseHeader("Content-Type")){
ret=dojo.dom.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
}
if(_743){
addToCache(url,_742,_73f.method,http);
}
_73f[(typeof _73f.load=="function")?"load":"handle"]("load",ret,http,_73f);
}else{
var _749=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_73f[(typeof _73f.error=="function")?"error":"handle"]("error",_749,http,_73f);
}
}
function setHeaders(http,_74b){
if(_74b["headers"]){
for(var _74c in _74b["headers"]){
if(_74c.toLowerCase()=="content-type"&&!_74b["contentType"]){
_74b["contentType"]=_74b["headers"][_74c];
}else{
http.setRequestHeader(_74c,_74b["headers"][_74c]);
}
}
}
}
this.inFlight=[];
this.inFlightTimer=null;
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
}
};
this.watchInFlight=function(){
var now=null;
if(!dojo.hostenv._blockAsync&&!_733._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
try{
var tif=this.inFlight[x];
if(!tif||tif.http._aborted||!tif.http.readyState){
this.inFlight.splice(x,1);
continue;
}
if(4==tif.http.readyState){
this.inFlight.splice(x,1);
doLoad(tif.req,tif.http,tif.url,tif.query,tif.useCache);
}else{
if(tif.startTime){
if(!now){
now=(new Date()).getTime();
}
if(tif.startTime+(tif.req.timeoutSeconds*1000)<now){
if(typeof tif.http.abort=="function"){
tif.http.abort();
}
this.inFlight.splice(x,1);
tif.req[(typeof tif.req.timeout=="function")?"timeout":"handle"]("timeout",null,tif.http,tif.req);
}
}
}
}
catch(e){
try{
var _750=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_750,tif.http,tif.req);
}
catch(e2){
dojo.debug("XMLHttpTransport error callback failed: "+e2);
}
}
}
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _751=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_752){
return _751&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_752["mimetype"].toLowerCase()||""))&&!(_752["formNode"]&&dojo.io.formHasFile(_752["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_753){
if(!_753["url"]){
if(!_753["formNode"]&&(_753["backButton"]||_753["back"]||_753["changeUrl"]||_753["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_753);
return true;
}
}
var url=_753.url;
var _755="";
if(_753["formNode"]){
var ta=_753.formNode.getAttribute("action");
if((ta)&&(!_753["url"])){
url=ta;
}
var tp=_753.formNode.getAttribute("method");
if((tp)&&(!_753["method"])){
_753.method=tp;
}
_755+=dojo.io.encodeForm(_753.formNode,_753.encoding,_753["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_753["file"]){
_753.method="post";
}
if(!_753["method"]){
_753.method="get";
}
if(_753.method.toLowerCase()=="get"){
_753.multipart=false;
}else{
if(_753["file"]){
_753.multipart=true;
}else{
if(!_753["multipart"]){
_753.multipart=false;
}
}
}
if(_753["backButton"]||_753["back"]||_753["changeUrl"]){
dojo.undo.browser.addToHistory(_753);
}
var _758=_753["content"]||{};
if(_753.sendTransport){
_758["dojo.transport"]="xmlhttp";
}
do{
if(_753.postContent){
_755=_753.postContent;
break;
}
if(_758){
_755+=dojo.io.argsFromMap(_758,_753.encoding);
}
if(_753.method.toLowerCase()=="get"||!_753.multipart){
break;
}
var t=[];
if(_755.length){
var q=_755.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_753.file){
if(dojo.lang.isArray(_753.file)){
for(var i=0;i<_753.file.length;++i){
var o=_753.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_753.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_755=t.join("\r\n");
}
}while(false);
var _75e=_753["sync"]?false:true;
var _75f=_753["preventCache"]||(this.preventCache==true&&_753["preventCache"]!=false);
var _760=_753["useCache"]==true||(this.useCache==true&&_753["useCache"]!=false);
if(!_75f&&_760){
var _761=getFromCache(url,_755,_753.method);
if(_761){
doLoad(_753,_761,url,_755,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_753);
var _763=false;
if(_75e){
var _764=this.inFlight.push({"req":_753,"http":http,"url":url,"query":_755,"useCache":_760,"startTime":_753.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_733._blockAsync=true;
}
if(_753.method.toLowerCase()=="post"){
if(!_753.user){
http.open("POST",url,_75e);
}else{
http.open("POST",url,_75e,_753.user,_753.password);
}
setHeaders(http,_753);
http.setRequestHeader("Content-Type",_753.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_753.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_755);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_753,{status:404},url,_755,_760);
}
}else{
var _765=url;
if(_755!=""){
_765+=(_765.indexOf("?")>-1?"&":"?")+_755;
}
if(_75f){
_765+=(dojo.string.endsWithAny(_765,"?","&")?"":(_765.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_753.user){
http.open(_753.method.toUpperCase(),_765,_75e);
}else{
http.open(_753.method.toUpperCase(),_765,_75e,_753.user,_753.password);
}
setHeaders(http,_753);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_753,{status:404},url,_755,_760);
}
}
if(!_75e){
doLoad(_753,http,url,_755,_760);
_733._blockAsync=false;
}
_753.abort=function(){
try{
http._aborted=true;
}
catch(e){
}
return http.abort();
};
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};
}
dojo.provide("dojo.uri.*");
dojo.provide("dojo.io.IframeIO");
dojo.io.createIFrame=function(_766,_767,uri){
if(window[_766]){
return window[_766];
}
if(window.frames[_766]){
return window.frames[_766];
}
var r=dojo.render.html;
var _76a=null;
var turi=uri||dojo.uri.dojoUri("iframe_history.html?noInit=true");
var _76c=((r.ie)&&(dojo.render.os.win))?"<iframe name=\""+_766+"\" src=\""+turi+"\" onload=\""+_767+"\">":"iframe";
_76a=document.createElement(_76c);
with(_76a){
name=_766;
setAttribute("name",_766);
id=_766;
}
dojo.body().appendChild(_76a);
window[_766]=_76a;
with(_76a.style){
if(!r.safari){
position="absolute";
}
left=top="0px";
height=width="1px";
visibility="hidden";
}
if(!r.ie){
dojo.io.setIFrameSrc(_76a,turi,true);
_76a.onload=new Function(_767);
}
return _76a;
};
dojo.io.IframeTransport=new function(){
var _76d=this;
this.currentRequest=null;
this.requestQueue=[];
this.iframeName="dojoIoIframe";
this.fireNextRequest=function(){
try{
if((this.currentRequest)||(this.requestQueue.length==0)){
return;
}
var cr=this.currentRequest=this.requestQueue.shift();
cr._contentToClean=[];
var fn=cr["formNode"];
var _770=cr["content"]||{};
if(cr.sendTransport){
_770["dojo.transport"]="iframe";
}
if(fn){
if(_770){
for(var x in _770){
if(!fn[x]){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_770[x]+"'>");
fn.appendChild(tn);
}else{
tn=document.createElement("input");
fn.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_770[x];
}
cr._contentToClean.push(x);
}else{
fn[x].value=_770[x];
}
}
}
if(cr["url"]){
cr._originalAction=fn.getAttribute("action");
fn.setAttribute("action",cr.url);
}
if(!fn.getAttribute("method")){
fn.setAttribute("method",(cr["method"])?cr["method"]:"post");
}
cr._originalTarget=fn.getAttribute("target");
fn.setAttribute("target",this.iframeName);
fn.target=this.iframeName;
fn.submit();
}else{
var _773=dojo.io.argsFromMap(this.currentRequest.content);
var _774=cr.url+(cr.url.indexOf("?")>-1?"&":"?")+_773;
dojo.io.setIFrameSrc(this.iframe,_774,true);
}
}
catch(e){
this.iframeOnload(e);
}
};
this.canHandle=function(_775){
return ((dojo.lang.inArray(["text/plain","text/html","text/javascript","text/json","application/json"],_775["mimetype"]))&&(dojo.lang.inArray(["post","get"],_775["method"].toLowerCase()))&&(!((_775["sync"])&&(_775["sync"]==true))));
};
this.bind=function(_776){
if(!this["iframe"]){
this.setUpIframe();
}
this.requestQueue.push(_776);
this.fireNextRequest();
return;
};
this.setUpIframe=function(){
this.iframe=dojo.io.createIFrame(this.iframeName,"dojo.io.IframeTransport.iframeOnload();");
};
this.iframeOnload=function(_777){
if(!_76d.currentRequest){
_76d.fireNextRequest();
return;
}
var req=_76d.currentRequest;
if(req.formNode){
var _779=req._contentToClean;
for(var i=0;i<_779.length;i++){
var key=_779[i];
if(dojo.render.html.safari){
var _77c=req.formNode;
for(var j=0;j<_77c.childNodes.length;j++){
var _77e=_77c.childNodes[j];
if(_77e.name==key){
var _77f=_77e.parentNode;
_77f.removeChild(_77e);
break;
}
}
}else{
var _780=req.formNode[key];
req.formNode.removeChild(_780);
req.formNode[key]=null;
}
}
if(req["_originalAction"]){
req.formNode.setAttribute("action",req._originalAction);
}
if(req["_originalTarget"]){
req.formNode.setAttribute("target",req._originalTarget);
req.formNode.target=req._originalTarget;
}
}
var _781=function(_782){
var doc=_782.contentDocument||((_782.contentWindow)&&(_782.contentWindow.document))||((_782.name)&&(document.frames[_782.name])&&(document.frames[_782.name].document))||null;
return doc;
};
var _784;
var _785=false;
if(_777){
this._callError(req,"IframeTransport Request Error: "+_777);
}else{
var ifd=_781(_76d.iframe);
try{
var cmt=req.mimetype;
if((cmt=="text/javascript")||(cmt=="text/json")||(cmt=="application/json")){
var js=ifd.getElementsByTagName("textarea")[0].value;
if(cmt=="text/json"||cmt=="application/json"){
js="("+js+")";
}
_784=dj_eval(js);
}else{
if(cmt=="text/html"){
_784=ifd;
}else{
_784=ifd.getElementsByTagName("textarea")[0].value;
}
}
_785=true;
}
catch(e){
this._callError(req,"IframeTransport Error: "+e);
}
}
try{
if(_785&&dojo.lang.isFunction(req["load"])){
req.load("load",_784,req);
}
}
catch(e){
throw e;
}
finally{
_76d.currentRequest=null;
_76d.fireNextRequest();
}
};
this._callError=function(req,_78a){
var _78b=new dojo.io.Error(_78a);
if(dojo.lang.isFunction(req["error"])){
req.error("error",_78b,req);
}
};
dojo.io.transports.addTransport("IframeTransport");
};
dojo.provide("dojo.io.ScriptSrcIO");
dojo.io.ScriptSrcTransport=new function(){
this.preventCache=false;
this.maxUrlLength=1000;
this.inFlightTimer=null;
this.DsrStatusCodes={Continue:100,Ok:200,Error:500};
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setInterval("dojo.io.ScriptSrcTransport.watchInFlight();",100);
}
};
this.watchInFlight=function(){
var _78c=0;
var _78d=0;
for(var _78e in this._state){
_78c++;
var _78f=this._state[_78e];
if(_78f.isDone){
_78d++;
delete this._state[_78e];
}else{
if(!_78f.isFinishing){
var _790=_78f.kwArgs;
try{
if(_78f.checkString&&eval("typeof("+_78f.checkString+") != 'undefined'")){
_78f.isFinishing=true;
this._finish(_78f,"load");
_78d++;
delete this._state[_78e];
}else{
if(_790.timeoutSeconds&&_790.timeout){
if(_78f.startTime+(_790.timeoutSeconds*1000)<(new Date()).getTime()){
_78f.isFinishing=true;
this._finish(_78f,"timeout");
_78d++;
delete this._state[_78e];
}
}else{
if(!_790.timeoutSeconds){
_78d++;
}
}
}
}
catch(e){
_78f.isFinishing=true;
this._finish(_78f,"error",{status:this.DsrStatusCodes.Error,response:e});
}
}
}
}
if(_78d>=_78c){
clearInterval(this.inFlightTimer);
this.inFlightTimer=null;
}
};
this.canHandle=function(_791){
return dojo.lang.inArray(["text/javascript","text/json","application/json"],(_791["mimetype"].toLowerCase()))&&(_791["method"].toLowerCase()=="get")&&!(_791["formNode"]&&dojo.io.formHasFile(_791["formNode"]))&&(!_791["sync"]||_791["sync"]==false)&&!_791["file"]&&!_791["multipart"];
};
this.removeScripts=function(){
var _792=document.getElementsByTagName("script");
for(var i=0;_792&&i<_792.length;i++){
var _794=_792[i];
if(_794.className=="ScriptSrcTransport"){
var _795=_794.parentNode;
_795.removeChild(_794);
i--;
}
}
};
this.bind=function(_796){
var url=_796.url;
var _798="";
if(_796["formNode"]){
var ta=_796.formNode.getAttribute("action");
if((ta)&&(!_796["url"])){
url=ta;
}
var tp=_796.formNode.getAttribute("method");
if((tp)&&(!_796["method"])){
_796.method=tp;
}
_798+=dojo.io.encodeForm(_796.formNode,_796.encoding,_796["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
var _79b=url.split("?");
if(_79b&&_79b.length==2){
url=_79b[0];
_798+=(_798?"&":"")+_79b[1];
}
if(_796["backButton"]||_796["back"]||_796["changeUrl"]){
dojo.undo.browser.addToHistory(_796);
}
var id=_796["apiId"]?_796["apiId"]:"id"+this._counter++;
var _79d=_796["content"];
var _79e=_796.jsonParamName;
if(_796.sendTransport||_79e){
if(!_79d){
_79d={};
}
if(_796.sendTransport){
_79d["dojo.transport"]="scriptsrc";
}
if(_79e){
_79d[_79e]="dojo.io.ScriptSrcTransport._state."+id+".jsonpCall";
}
}
if(_796.postContent){
_798=_796.postContent;
}else{
if(_79d){
_798+=((_798)?"&":"")+dojo.io.argsFromMap(_79d,_796.encoding,_79e);
}
}
if(_796["apiId"]){
_796["useRequestId"]=true;
}
var _79f={"id":id,"idParam":"_dsrid="+id,"url":url,"query":_798,"kwArgs":_796,"startTime":(new Date()).getTime(),"isFinishing":false};
if(!url){
this._finish(_79f,"error",{status:this.DsrStatusCodes.Error,statusText:"url.none"});
return;
}
if(_79d&&_79d[_79e]){
_79f.jsonp=_79d[_79e];
_79f.jsonpCall=function(data){
if(data["Error"]||data["error"]){
if(dojo["json"]&&dojo["json"]["serialize"]){
dojo.debug(dojo.json.serialize(data));
}
dojo.io.ScriptSrcTransport._finish(this,"error",data);
}else{
dojo.io.ScriptSrcTransport._finish(this,"load",data);
}
};
}
if(_796["useRequestId"]||_796["checkString"]||_79f["jsonp"]){
this._state[id]=_79f;
}
if(_796["checkString"]){
_79f.checkString=_796["checkString"];
}
_79f.constantParams=(_796["constantParams"]==null?"":_796["constantParams"]);
if(_796["preventCache"]||(this.preventCache==true&&_796["preventCache"]!=false)){
_79f.nocacheParam="dojo.preventCache="+new Date().valueOf();
}else{
_79f.nocacheParam="";
}
var _7a1=_79f.url.length+_79f.query.length+_79f.constantParams.length+_79f.nocacheParam.length+this._extraPaddingLength;
if(_796["useRequestId"]){
_7a1+=_79f.idParam.length;
}
if(!_796["checkString"]&&_796["useRequestId"]&&!_79f["jsonp"]&&!_796["forceSingleRequest"]&&_7a1>this.maxUrlLength){
if(url>this.maxUrlLength){
this._finish(_79f,"error",{status:this.DsrStatusCodes.Error,statusText:"url.tooBig"});
return;
}else{
this._multiAttach(_79f,1);
}
}else{
var _7a2=[_79f.constantParams,_79f.nocacheParam,_79f.query];
if(_796["useRequestId"]&&!_79f["jsonp"]){
_7a2.unshift(_79f.idParam);
}
var _7a3=this._buildUrl(_79f.url,_7a2);
_79f.finalUrl=_7a3;
this._attach(_79f.id,_7a3);
}
this.startWatchingInFlight();
};
this._counter=1;
this._state={};
this._extraPaddingLength=16;
this._buildUrl=function(url,_7a5){
var _7a6=url;
var _7a7="?";
for(var i=0;i<_7a5.length;i++){
if(_7a5[i]){
_7a6+=_7a7+_7a5[i];
_7a7="&";
}
}
return _7a6;
};
this._attach=function(id,url){
var _7ab=document.createElement("script");
_7ab.type="text/javascript";
_7ab.src=url;
_7ab.id=id;
_7ab.className="ScriptSrcTransport";
document.getElementsByTagName("head")[0].appendChild(_7ab);
};
this._multiAttach=function(_7ac,part){
if(_7ac.query==null){
this._finish(_7ac,"error",{status:this.DsrStatusCodes.Error,statusText:"query.null"});
return;
}
if(!_7ac.constantParams){
_7ac.constantParams="";
}
var _7ae=this.maxUrlLength-_7ac.idParam.length-_7ac.constantParams.length-_7ac.url.length-_7ac.nocacheParam.length-this._extraPaddingLength;
var _7af=_7ac.query.length<_7ae;
var _7b0;
if(_7af){
_7b0=_7ac.query;
_7ac.query=null;
}else{
var _7b1=_7ac.query.lastIndexOf("&",_7ae-1);
var _7b2=_7ac.query.lastIndexOf("=",_7ae-1);
if(_7b1>_7b2||_7b2==_7ae-1){
_7b0=_7ac.query.substring(0,_7b1);
_7ac.query=_7ac.query.substring(_7b1+1,_7ac.query.length);
}else{
_7b0=_7ac.query.substring(0,_7ae);
var _7b3=_7b0.substring((_7b1==-1?0:_7b1+1),_7b2);
_7ac.query=_7b3+"="+_7ac.query.substring(_7ae,_7ac.query.length);
}
}
var _7b4=[_7b0,_7ac.idParam,_7ac.constantParams,_7ac.nocacheParam];
if(!_7af){
_7b4.push("_part="+part);
}
var url=this._buildUrl(_7ac.url,_7b4);
this._attach(_7ac.id+"_"+part,url);
};
this._finish=function(_7b6,_7b7,_7b8){
if(_7b7!="partOk"&&!_7b6.kwArgs[_7b7]&&!_7b6.kwArgs["handle"]){
if(_7b7=="error"){
_7b6.isDone=true;
throw _7b8;
}
}else{
switch(_7b7){
case "load":
var _7b9=_7b8?_7b8.response:null;
if(!_7b9){
_7b9=_7b8;
}
_7b6.kwArgs[(typeof _7b6.kwArgs.load=="function")?"load":"handle"]("load",_7b9,_7b8,_7b6.kwArgs);
_7b6.isDone=true;
break;
case "partOk":
var part=parseInt(_7b8.response.part,10)+1;
if(_7b8.response.constantParams){
_7b6.constantParams=_7b8.response.constantParams;
}
this._multiAttach(_7b6,part);
_7b6.isDone=false;
break;
case "error":
_7b6.kwArgs[(typeof _7b6.kwArgs.error=="function")?"error":"handle"]("error",_7b8.response,_7b8,_7b6.kwArgs);
_7b6.isDone=true;
break;
default:
_7b6.kwArgs[(typeof _7b6.kwArgs[_7b7]=="function")?_7b7:"handle"](_7b7,_7b8,_7b8,_7b6.kwArgs);
_7b6.isDone=true;
}
}
};
dojo.io.transports.addTransport("ScriptSrcTransport");
};
if(typeof window!="undefined"){
window.onscriptload=function(_7bb){
var _7bc=null;
var _7bd=dojo.io.ScriptSrcTransport;
if(_7bd._state[_7bb.id]){
_7bc=_7bd._state[_7bb.id];
}else{
var _7be;
for(var _7bf in _7bd._state){
_7be=_7bd._state[_7bf];
if(_7be.finalUrl&&_7be.finalUrl==_7bb.id){
_7bc=_7be;
break;
}
}
if(_7bc==null){
var _7c0=document.getElementsByTagName("script");
for(var i=0;_7c0&&i<_7c0.length;i++){
var _7c2=_7c0[i];
if(_7c2.getAttribute("class")=="ScriptSrcTransport"&&_7c2.src==_7bb.id){
_7bc=_7bd._state[_7c2.id];
break;
}
}
}
if(_7bc==null){
throw "No matching state for onscriptload event.id: "+_7bb.id;
}
}
var _7c3="error";
switch(_7bb.status){
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Continue:
_7c3="partOk";
break;
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Ok:
_7c3="load";
break;
}
_7bd._finish(_7bc,_7c3,_7bb);
};
}
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_7c5,days,path,_7c8,_7c9){
var _7ca=-1;
if((typeof days=="number")&&(days>=0)){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_7ca=d.toGMTString();
}
_7c5=escape(_7c5);
document.cookie=name+"="+_7c5+";"+(_7ca!=-1?" expires="+_7ca+";":"")+(path?"path="+path:"")+(_7c8?"; domain="+_7c8:"")+(_7c9?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _7ce=document.cookie.substring(idx+name.length+1);
var end=_7ce.indexOf(";");
if(end==-1){
end=_7ce.length;
}
_7ce=_7ce.substring(0,end);
_7ce=unescape(_7ce);
return _7ce;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_7d5,_7d6,_7d7){
if(arguments.length==5){
_7d7=_7d5;
_7d5=null;
_7d6=null;
}
var _7d8=[],_7d9,_7da="";
if(!_7d7){
_7d9=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_7d9){
_7d9={};
}
for(var prop in obj){
if(obj[prop]==null){
delete _7d9[prop];
}else{
if((typeof obj[prop]=="string")||(typeof obj[prop]=="number")){
_7d9[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _7d9){
_7d8.push(escape(prop)+"="+escape(_7d9[prop]));
}
_7da=_7d8.join("&");
}
dojo.io.cookie.setCookie(name,_7da,days,path,_7d5,_7d6);
};
dojo.io.cookie.getObjectCookie=function(name){
var _7dd=null,_7de=dojo.io.cookie.getCookie(name);
if(_7de){
_7dd={};
var _7df=_7de.split("&");
for(var i=0;i<_7df.length;i++){
var pair=_7df[i].split("=");
var _7e2=pair[1];
if(isNaN(_7e2)){
_7e2=unescape(pair[1]);
}
_7dd[unescape(pair[0])]=_7e2;
}
}
return _7dd;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _7e3=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_7e3=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.provide("dojo.io.cometd");
cometd=new function(){
this.initialized=false;
this.connected=false;
this.connectionTypes=new dojo.AdapterRegistry(true);
this.version=0.1;
this.minimumVersion=0.1;
this.clientId=null;
this._isXD=false;
this.handshakeReturn=null;
this.currentTransport=null;
this.url=null;
this.lastMessage=null;
this.globalTopicChannels={};
this.backlog=[];
this.tunnelInit=function(_7e4,_7e5){
};
this.tunnelCollapse=function(){
dojo.debug("tunnel collapsed!");
};
this.init=function(_7e6,root,_7e8){
_7e6=_7e6||{};
_7e6.version=this.version;
_7e6.minimumVersion=this.minimumVersion;
_7e6.channel="/meta/handshake";
this.url=root||djConfig["cometdRoot"];
if(!this.url){
dojo.debug("no cometd root specified in djConfig and no root passed");
return;
}
var _7e9={url:this.url,method:"POST",mimetype:"text/json",load:dojo.lang.hitch(this,"finishInit"),content:{"message":dojo.json.serialize([_7e6])}};
var _7ea="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=(""+window.location).match(new RegExp(_7ea));
if(r[4]){
var tmp=r[4].split(":");
var _7ed=tmp[0];
var _7ee=tmp[1]||"80";
r=this.url.match(new RegExp(_7ea));
if(r[4]){
tmp=r[4].split(":");
var _7ef=tmp[0];
var _7f0=tmp[1]||"80";
if((_7ef!=_7ed)||(_7f0!=_7ee)){
dojo.debug(_7ed,_7ef);
dojo.debug(_7ee,_7f0);
this._isXD=true;
_7e9.transport="ScriptSrcTransport";
_7e9.jsonParamName="jsonp";
_7e9.method="GET";
}
}
}
if(_7e8){
dojo.lang.mixin(_7e9,_7e8);
}
return dojo.io.bind(_7e9);
};
this.finishInit=function(type,data,evt,_7f4){
data=data[0];
this.handshakeReturn=data;
if(data["authSuccessful"]==false){
dojo.debug("cometd authentication failed");
return;
}
if(data.version<this.minimumVersion){
dojo.debug("cometd protocol version mismatch. We wanted",this.minimumVersion,"but got",data.version);
return;
}
this.currentTransport=this.connectionTypes.match(data.supportedConnectionTypes,data.version,this._isXD);
this.currentTransport.version=data.version;
this.clientId=data.clientId;
this.tunnelInit=dojo.lang.hitch(this.currentTransport,"tunnelInit");
this.tunnelCollapse=dojo.lang.hitch(this.currentTransport,"tunnelCollapse");
this.initialized=true;
this.currentTransport.startup(data);
while(this.backlog.length!=0){
var cur=this.backlog.shift();
var fn=cur.shift();
this[fn].apply(this,cur);
}
};
this._getRandStr=function(){
return Math.random().toString().substring(2,10);
};
this.deliver=function(_7f7){
dojo.lang.forEach(_7f7,this._deliver,this);
};
this._deliver=function(_7f8){
if(!_7f8["channel"]){
dojo.debug("cometd error: no channel for message!");
return;
}
if(!this.currentTransport){
this.backlog.push(["deliver",_7f8]);
return;
}
this.lastMessage=_7f8;
if((_7f8.channel.length>5)&&(_7f8.channel.substr(0,5)=="/meta")){
switch(_7f8.channel){
case "/meta/subscribe":
if(!_7f8.successful){
dojo.debug("cometd subscription error for channel",_7f8.channel,":",_7f8.error);
return;
}
this.subscribed(_7f8.subscription,_7f8);
break;
case "/meta/unsubscribe":
if(!_7f8.successful){
dojo.debug("cometd unsubscription error for channel",_7f8.channel,":",_7f8.error);
return;
}
this.unsubscribed(_7f8.subscription,_7f8);
break;
}
}
this.currentTransport.deliver(_7f8);
var _7f9=(this.globalTopicChannels[_7f8.channel])?_7f8.channel:"/cometd"+_7f8.channel;
dojo.event.topic.publish(_7f9,_7f8);
};
this.disconnect=function(){
if(!this.currentTransport){
dojo.debug("no current transport to disconnect from");
return;
}
this.currentTransport.disconnect();
};
this.publish=function(_7fa,data,_7fc){
if(!this.currentTransport){
this.backlog.push(["publish",_7fa,data,_7fc]);
return;
}
var _7fd={data:data,channel:_7fa};
if(_7fc){
dojo.lang.mixin(_7fd,_7fc);
}
return this.currentTransport.sendMessage(_7fd);
};
this.subscribe=function(_7fe,_7ff,_800,_801){
if(!this.currentTransport){
this.backlog.push(["subscribe",_7fe,_7ff,_800,_801]);
return;
}
if(_800){
var _802=(_7ff)?_7fe:"/cometd"+_7fe;
if(_7ff){
this.globalTopicChannels[_7fe]=true;
}
dojo.event.topic.subscribe(_802,_800,_801);
}
return this.currentTransport.sendMessage({channel:"/meta/subscribe",subscription:_7fe});
};
this.subscribed=function(_803,_804){
dojo.debug(_803);
dojo.debugShallow(_804);
};
this.unsubscribe=function(_805,_806,_807,_808){
if(!this.currentTransport){
this.backlog.push(["unsubscribe",_805,_806,_807,_808]);
return;
}
if(_807){
var _809=(_806)?_805:"/cometd"+_805;
dojo.event.topic.unsubscribe(_809,_807,_808);
}
return this.currentTransport.sendMessage({channel:"/meta/unsubscribe",subscription:_805});
};
this.unsubscribed=function(_80a,_80b){
dojo.debug(_80a);
dojo.debugShallow(_80b);
};
};
cometd.iframeTransport=new function(){
this.connected=false;
this.connectionId=null;
this.rcvNode=null;
this.rcvNodeName="";
this.phonyForm=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_80c,_80d,_80e){
return ((!_80e)&&(!dojo.render.html.safari)&&(dojo.lang.inArray(_80c,"iframe")));
};
this.tunnelInit=function(){
this.postToIframe({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"iframe"}])});
};
this.tunnelCollapse=function(){
if(this.connected){
this.connected=false;
this.postToIframe({message:dojo.json.serialize([{channel:"/meta/reconnect",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=function(_80f){
if(_80f["timestamp"]){
this.lastTimestamp=_80f.timestamp;
}
if(_80f["id"]){
this.lastId=_80f.id;
}
if((_80f.channel.length>5)&&(_80f.channel.substr(0,5)=="/meta")){
switch(_80f.channel){
case "/meta/connect":
if(!_80f.successful){
dojo.debug("cometd connection error:",_80f.error);
return;
}
this.connectionId=_80f.connectionId;
this.connected=true;
this.processBacklog();
break;
case "/meta/reconnect":
if(!_80f.successful){
dojo.debug("cometd reconnection error:",_80f.error);
return;
}
this.connected=true;
break;
case "/meta/subscribe":
if(!_80f.successful){
dojo.debug("cometd subscription error for channel",_80f.channel,":",_80f.error);
return;
}
dojo.debug(_80f.channel);
break;
}
}
};
this.widenDomain=function(_810){
var cd=_810||document.domain;
if(cd.indexOf(".")==-1){
return;
}
var dps=cd.split(".");
if(dps.length<=2){
return;
}
dps=dps.slice(dps.length-2);
document.domain=dps.join(".");
return document.domain;
};
this.postToIframe=function(_813,url){
if(!this.phonyForm){
if(dojo.render.html.ie){
this.phonyForm=document.createElement("<form enctype='application/x-www-form-urlencoded' method='POST' style='display: none;'>");
dojo.body().appendChild(this.phonyForm);
}else{
this.phonyForm=document.createElement("form");
this.phonyForm.style.display="none";
dojo.body().appendChild(this.phonyForm);
this.phonyForm.enctype="application/x-www-form-urlencoded";
this.phonyForm.method="POST";
}
}
this.phonyForm.action=url||cometd.url;
this.phonyForm.target=this.rcvNodeName;
this.phonyForm.setAttribute("target",this.rcvNodeName);
while(this.phonyForm.firstChild){
this.phonyForm.removeChild(this.phonyForm.firstChild);
}
for(var x in _813){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_813[x]+"'>");
this.phonyForm.appendChild(tn);
}else{
tn=document.createElement("input");
this.phonyForm.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_813[x];
}
}
this.phonyForm.submit();
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_817,_818){
if((_818)||(this.connected)){
_817.connectionId=this.connectionId;
_817.clientId=cometd.clientId;
var _819={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_817])}};
return dojo.io.bind(_819);
}else{
this.backlog.push(_817);
}
};
this.startup=function(_81a){
dojo.debug("startup!");
dojo.debug(dojo.json.serialize(_81a));
if(this.connected){
return;
}
this.rcvNodeName="cometdRcv_"+cometd._getRandStr();
var _81b=cometd.url+"/?tunnelInit=iframe";
if(false&&dojo.render.html.ie){
this.rcvNode=new ActiveXObject("htmlfile");
this.rcvNode.open();
this.rcvNode.write("<html>");
this.rcvNode.write("<script>document.domain = '"+document.domain+"'");
this.rcvNode.write("</html>");
this.rcvNode.close();
var _81c=this.rcvNode.createElement("div");
this.rcvNode.appendChild(_81c);
this.rcvNode.parentWindow.dojo=dojo;
_81c.innerHTML="<iframe src='"+_81b+"'></iframe>";
}else{
this.rcvNode=dojo.io.createIFrame(this.rcvNodeName,"",_81b);
}
};
};
cometd.mimeReplaceTransport=new function(){
this.connected=false;
this.connectionId=null;
this.xhr=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_81d,_81e,_81f){
return ((!_81f)&&(dojo.render.html.mozilla)&&(dojo.lang.inArray(_81d,"mime-message-block")));
};
this.tunnelInit=function(){
if(this.connected){
return;
}
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"mime-message-block"}])});
this.connected=true;
};
this.tunnelCollapse=function(){
if(this.connected){
this.connected=false;
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/reconnect",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=cometd.iframeTransport.deliver;
this.handleOnLoad=function(resp){
cometd.deliver(dojo.json.evalJson(this.xhr.responseText));
};
this.openTunnelWith=function(_821,url){
this.xhr=dojo.hostenv.getXmlhttpObject();
this.xhr.multipart=true;
if(dojo.render.html.mozilla){
this.xhr.addEventListener("load",dojo.lang.hitch(this,"handleOnLoad"),false);
}else{
if(dojo.render.html.safari){
dojo.debug("Webkit is broken with multipart responses over XHR = (");
this.xhr.onreadystatechange=dojo.lang.hitch(this,"handleOnLoad");
}else{
this.xhr.onload=dojo.lang.hitch(this,"handleOnLoad");
}
}
this.xhr.open("POST",(url||cometd.url),true);
this.xhr.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
dojo.debug(dojo.json.serialize(_821));
this.xhr.send(dojo.io.argsFromMap(_821,"utf8"));
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_823,_824){
if((_824)||(this.connected)){
_823.connectionId=this.connectionId;
_823.clientId=cometd.clientId;
var _825={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_823])}};
return dojo.io.bind(_825);
}else{
this.backlog.push(_823);
}
};
this.startup=function(_826){
dojo.debugShallow(_826);
if(this.connected){
return;
}
this.tunnelInit();
};
};
cometd.longPollTransport=new function(){
this.connected=false;
this.connectionId=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_827,_828,_829){
return ((!_829)&&(dojo.lang.inArray(_827,"long-polling")));
};
this.tunnelInit=function(){
if(this.connected){
return;
}
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"long-polling"}])});
this.connected=true;
};
this.tunnelCollapse=function(){
if(!this.connected){
this.connected=false;
dojo.debug("clientId:",cometd.clientId);
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/reconnect",connectionType:"long-polling",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=cometd.iframeTransport.deliver;
this.openTunnelWith=function(_82a,url){
dojo.io.bind({url:(url||cometd.url),method:"post",content:_82a,mimetype:"text/json",load:dojo.lang.hitch(this,function(type,data,evt,args){
cometd.deliver(data);
this.connected=false;
this.tunnelCollapse();
}),error:function(){
dojo.debug("tunnel opening failed");
}});
this.connected=true;
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_830,_831){
if((_831)||(this.connected)){
_830.connectionId=this.connectionId;
_830.clientId=cometd.clientId;
var _832={url:cometd.url||djConfig["cometdRoot"],method:"post",mimetype:"text/json",content:{message:dojo.json.serialize([_830])}};
return dojo.io.bind(_832);
}else{
this.backlog.push(_830);
}
};
this.startup=function(_833){
if(this.connected){
return;
}
this.tunnelInit();
};
};
cometd.callbackPollTransport=new function(){
this.connected=false;
this.connectionId=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_834,_835,_836){
return dojo.lang.inArray(_834,"callback-polling");
};
this.tunnelInit=function(){
if(this.connected){
return;
}
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"callback-polling"}])});
this.connected=true;
};
this.tunnelCollapse=function(){
if(!this.connected){
this.connected=false;
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/reconnect",connectionType:"long-polling",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=cometd.iframeTransport.deliver;
this.openTunnelWith=function(_837,url){
var req=dojo.io.bind({url:(url||cometd.url),content:_837,mimetype:"text/json",transport:"ScriptSrcTransport",jsonParamName:"jsonp",load:dojo.lang.hitch(this,function(type,data,evt,args){
dojo.debug(dojo.json.serialize(data));
cometd.deliver(data);
this.connected=false;
this.tunnelCollapse();
}),error:function(){
dojo.debug("tunnel opening failed");
}});
this.connected=true;
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_83e,_83f){
if((_83f)||(this.connected)){
_83e.connectionId=this.connectionId;
_83e.clientId=cometd.clientId;
var _840={url:cometd.url||djConfig["cometdRoot"],mimetype:"text/json",transport:"ScriptSrcTransport",jsonParamName:"jsonp",content:{message:dojo.json.serialize([_83e])}};
return dojo.io.bind(_840);
}else{
this.backlog.push(_83e);
}
};
this.startup=function(_841){
if(this.connected){
return;
}
this.tunnelInit();
};
};
cometd.connectionTypes.register("mime-message-block",cometd.mimeReplaceTransport.check,cometd.mimeReplaceTransport);
cometd.connectionTypes.register("long-polling",cometd.longPollTransport.check,cometd.longPollTransport);
cometd.connectionTypes.register("callback-polling",cometd.callbackPollTransport.check,cometd.callbackPollTransport);
cometd.connectionTypes.register("iframe",cometd.iframeTransport.check,cometd.iframeTransport);
dojo.io.cometd=cometd;
dojo.provide("dojo.xml.Parse");
dojo.xml.Parse=function(){
var isIE=((dojo.render.html.capable)&&(dojo.render.html.ie));
function getTagName(node){
try{
return node.tagName.toLowerCase();
}
catch(e){
return "";
}
}
function getDojoTagName(node){
var _845=getTagName(node);
if(!_845){
return "";
}
if((dojo.widget)&&(dojo.widget.tags[_845])){
return _845;
}
var p=_845.indexOf(":");
if(p>=0){
return _845;
}
if(_845.substr(0,5)=="dojo:"){
return _845;
}
if(dojo.render.html.capable&&dojo.render.html.ie&&node.scopeName!="HTML"){
return node.scopeName.toLowerCase()+":"+_845;
}
if(_845.substr(0,4)=="dojo"){
return "dojo:"+_845.substring(4);
}
var djt=node.getAttribute("dojoType")||node.getAttribute("dojotype");
if(djt){
if(djt.indexOf(":")<0){
djt="dojo:"+djt;
}
return djt.toLowerCase();
}
djt=node.getAttributeNS&&node.getAttributeNS(dojo.dom.dojoml,"type");
if(djt){
return "dojo:"+djt.toLowerCase();
}
try{
djt=node.getAttribute("dojo:type");
}
catch(e){
}
if(djt){
return "dojo:"+djt.toLowerCase();
}
if((dj_global["djConfig"])&&(!djConfig["ignoreClassNames"])){
var _848=node.className||node.getAttribute("class");
if((_848)&&(_848.indexOf)&&(_848.indexOf("dojo-")!=-1)){
var _849=_848.split(" ");
for(var x=0,c=_849.length;x<c;x++){
if(_849[x].slice(0,5)=="dojo-"){
return "dojo:"+_849[x].substr(5).toLowerCase();
}
}
}
}
return "";
}
this.parseElement=function(node,_84d,_84e,_84f){
var _850=getTagName(node);
if(isIE&&_850.indexOf("/")==0){
return null;
}
try{
var attr=node.getAttribute("parseWidgets");
if(attr&&attr.toLowerCase()=="false"){
return {};
}
}
catch(e){
}
var _852=true;
if(_84e){
var _853=getDojoTagName(node);
_850=_853||_850;
_852=Boolean(_853);
}
var _854={};
_854[_850]=[];
var pos=_850.indexOf(":");
if(pos>0){
var ns=_850.substring(0,pos);
_854["ns"]=ns;
if((dojo.ns)&&(!dojo.ns.allow(ns))){
_852=false;
}
}
if(_852){
var _857=this.parseAttributes(node);
for(var attr in _857){
if((!_854[_850][attr])||(typeof _854[_850][attr]!="array")){
_854[_850][attr]=[];
}
_854[_850][attr].push(_857[attr]);
}
_854[_850].nodeRef=node;
_854.tagName=_850;
_854.index=_84f||0;
}
var _858=0;
for(var i=0;i<node.childNodes.length;i++){
var tcn=node.childNodes.item(i);
switch(tcn.nodeType){
case dojo.dom.ELEMENT_NODE:
var ctn=getDojoTagName(tcn)||getTagName(tcn);
if(!_854[ctn]){
_854[ctn]=[];
}
_854[ctn].push(this.parseElement(tcn,true,_84e,_858));
if((tcn.childNodes.length==1)&&(tcn.childNodes.item(0).nodeType==dojo.dom.TEXT_NODE)){
_854[ctn][_854[ctn].length-1].value=tcn.childNodes.item(0).nodeValue;
}
_858++;
break;
case dojo.dom.TEXT_NODE:
if(node.childNodes.length==1){
_854[_850].push({value:node.childNodes.item(0).nodeValue});
}
break;
default:
break;
}
}
return _854;
};
this.parseAttributes=function(node){
var _85d={};
var atts=node.attributes;
var _85f,i=0;
while((_85f=atts[i++])){
if(isIE){
if(!_85f){
continue;
}
if((typeof _85f=="object")&&(typeof _85f.nodeValue=="undefined")||(_85f.nodeValue==null)||(_85f.nodeValue=="")){
continue;
}
}
var nn=_85f.nodeName.split(":");
nn=(nn.length==2)?nn[1]:_85f.nodeName;
_85d[nn]={value:_85f.nodeValue};
}
return _85d;
};
};
dojo.provide("dojo.widget.Manager");
dojo.widget.manager=new function(){
this.widgets=[];
this.widgetIds=[];
this.topWidgets={};
var _862={};
var _863=[];
this.getUniqueId=function(_864){
var _865;
do{
_865=_864+"_"+(_862[_864]!=undefined?++_862[_864]:_862[_864]=0);
}while(this.getWidgetById(_865));
return _865;
};
this.add=function(_866){
this.widgets.push(_866);
if(!_866.extraArgs["id"]){
_866.extraArgs["id"]=_866.extraArgs["ID"];
}
if(_866.widgetId==""){
if(_866["id"]){
_866.widgetId=_866["id"];
}else{
if(_866.extraArgs["id"]){
_866.widgetId=_866.extraArgs["id"];
}else{
_866.widgetId=this.getUniqueId(_866.ns+"_"+_866.widgetType);
}
}
}
if(this.widgetIds[_866.widgetId]){
dojo.debug("widget ID collision on ID: "+_866.widgetId);
}
this.widgetIds[_866.widgetId]=_866;
};
this.destroyAll=function(){
for(var x=this.widgets.length-1;x>=0;x--){
try{
this.widgets[x].destroy(true);
delete this.widgets[x];
}
catch(e){
}
}
};
this.remove=function(_868){
if(dojo.lang.isNumber(_868)){
var tw=this.widgets[_868].widgetId;
delete this.widgetIds[tw];
this.widgets.splice(_868,1);
}else{
this.removeById(_868);
}
};
this.removeById=function(id){
if(!dojo.lang.isString(id)){
id=id["widgetId"];
if(!id){
dojo.debug("invalid widget or id passed to removeById");
return;
}
}
for(var i=0;i<this.widgets.length;i++){
if(this.widgets[i].widgetId==id){
this.remove(i);
break;
}
}
};
this.getWidgetById=function(id){
if(dojo.lang.isString(id)){
return this.widgetIds[id];
}
return id;
};
this.getWidgetsByType=function(type){
var lt=type.toLowerCase();
var _86f=(type.indexOf(":")<0?function(x){
return x.widgetType.toLowerCase();
}:function(x){
return x.getNamespacedType();
});
var ret=[];
dojo.lang.forEach(this.widgets,function(x){
if(_86f(x)==lt){
ret.push(x);
}
});
return ret;
};
this.getWidgetsByFilter=function(_874,_875){
var ret=[];
dojo.lang.every(this.widgets,function(x){
if(_874(x)){
ret.push(x);
if(_875){
return false;
}
}
return true;
});
return (_875?ret[0]:ret);
};
this.getAllWidgets=function(){
return this.widgets.concat();
};
this.getWidgetByNode=function(node){
var w=this.getAllWidgets();
node=dojo.byId(node);
for(var i=0;i<w.length;i++){
if(w[i].domNode==node){
return w[i];
}
}
return null;
};
this.byId=this.getWidgetById;
this.byType=this.getWidgetsByType;
this.byFilter=this.getWidgetsByFilter;
this.byNode=this.getWidgetByNode;
var _87b={};
var _87c=["dojo.widget"];
for(var i=0;i<_87c.length;i++){
_87c[_87c[i]]=true;
}
this.registerWidgetPackage=function(_87e){
if(!_87c[_87e]){
_87c[_87e]=true;
_87c.push(_87e);
}
};
this.getWidgetPackageList=function(){
return dojo.lang.map(_87c,function(elt){
return (elt!==true?elt:undefined);
});
};
this.getImplementation=function(_880,_881,_882,ns){
var impl=this.getImplementationName(_880,ns);
if(impl){
var ret=_881?new impl(_881):new impl();
return ret;
}
};
function buildPrefixCache(){
for(var _886 in dojo.render){
if(dojo.render[_886]["capable"]===true){
var _887=dojo.render[_886].prefixes;
for(var i=0;i<_887.length;i++){
_863.push(_887[i].toLowerCase());
}
}
}
}
var _889=function(_88a,_88b){
if(!_88b){
return null;
}
for(var i=0,l=_863.length,_88e;i<=l;i++){
_88e=(i<l?_88b[_863[i]]:_88b);
if(!_88e){
continue;
}
for(var name in _88e){
if(name.toLowerCase()==_88a){
return _88e[name];
}
}
}
return null;
};
var _890=function(_891,_892){
var _893=dojo.evalObjPath(_892,false);
return (_893?_889(_891,_893):null);
};
this.getImplementationName=function(_894,ns){
var _896=_894.toLowerCase();
ns=ns||"dojo";
var imps=_87b[ns]||(_87b[ns]={});
var impl=imps[_896];
if(impl){
return impl;
}
if(!_863.length){
buildPrefixCache();
}
var _899=dojo.ns.get(ns);
if(!_899){
dojo.ns.register(ns,ns+".widget");
_899=dojo.ns.get(ns);
}
if(_899){
_899.resolve(_894);
}
impl=_890(_896,_899.module);
if(impl){
return (imps[_896]=impl);
}
_899=dojo.ns.require(ns);
if((_899)&&(_899.resolver)){
_899.resolve(_894);
impl=_890(_896,_899.module);
if(impl){
return (imps[_896]=impl);
}
}
dojo.deprecated("dojo.widget.Manager.getImplementationName","Could not locate widget implementation for \""+_894+"\" in \""+_899.module+"\" registered to namespace \""+_899.name+"\". "+"Developers must specify correct namespaces for all non-Dojo widgets","0.5");
for(var i=0;i<_87c.length;i++){
impl=_890(_896,_87c[i]);
if(impl){
return (imps[_896]=impl);
}
}
throw new Error("Could not locate widget implementation for \""+_894+"\" in \""+_899.module+"\" registered to namespace \""+_899.name+"\"");
};
this.resizing=false;
this.onWindowResized=function(){
if(this.resizing){
return;
}
try{
this.resizing=true;
for(var id in this.topWidgets){
var _89c=this.topWidgets[id];
if(_89c.checkSize){
_89c.checkSize();
}
}
}
catch(e){
}
finally{
this.resizing=false;
}
};
if(typeof window!="undefined"){
dojo.addOnLoad(this,"onWindowResized");
dojo.event.connect(window,"onresize",this,"onWindowResized");
}
};
(function(){
var dw=dojo.widget;
var dwm=dw.manager;
var h=dojo.lang.curry(dojo.lang,"hitch",dwm);
var g=function(_8a1,_8a2){
dw[(_8a2||_8a1)]=h(_8a1);
};
g("add","addWidget");
g("destroyAll","destroyAllWidgets");
g("remove","removeWidget");
g("removeById","removeWidgetById");
g("getWidgetById");
g("getWidgetById","byId");
g("getWidgetsByType");
g("getWidgetsByFilter");
g("getWidgetsByType","byType");
g("getWidgetsByFilter","byFilter");
g("getWidgetByNode","byNode");
dw.all=function(n){
var _8a4=dwm.getAllWidgets.apply(dwm,arguments);
if(arguments.length>0){
return _8a4[n];
}
return _8a4;
};
g("registerWidgetPackage");
g("getImplementation","getWidgetImplementation");
g("getImplementationName","getWidgetImplementationName");
dw.widgets=dwm.widgets;
dw.widgetIds=dwm.widgetIds;
dw.root=dwm.root;
})();
dojo.provide("dojo.a11y");
dojo.a11y={imgPath:dojo.uri.dojoUri("src/widget/templates/images"),doAccessibleCheck:true,accessible:null,checkAccessible:function(){
if(this.accessible===null){
this.accessible=false;
if(this.doAccessibleCheck==true){
this.accessible=this.testAccessible();
}
}
return this.accessible;
},testAccessible:function(){
this.accessible=false;
if(dojo.render.html.ie||dojo.render.html.mozilla){
var div=document.createElement("div");
div.style.backgroundImage="url(\""+this.imgPath+"/tab_close.gif\")";
dojo.body().appendChild(div);
var _8a6=null;
if(window.getComputedStyle){
var _8a7=getComputedStyle(div,"");
_8a6=_8a7.getPropertyValue("background-image");
}else{
_8a6=div.currentStyle.backgroundImage;
}
var _8a8=false;
if(_8a6!=null&&(_8a6=="none"||_8a6=="url(invalid-url:)")){
this.accessible=true;
}
dojo.body().removeChild(div);
}
return this.accessible;
},setCheckAccessible:function(_8a9){
this.doAccessibleCheck=_8a9;
},setAccessibleMode:function(){
if(this.accessible===null){
if(this.checkAccessible()){
dojo.render.html.prefixes.unshift("a11y");
}
}
return this.accessible;
}};
dojo.provide("dojo.widget.Widget");
dojo.declare("dojo.widget.Widget",null,function(){
this.children=[];
this.extraArgs={};
},{parent:null,isTopLevel:false,disabled:false,isContainer:false,widgetId:"",widgetType:"Widget",ns:"dojo",getNamespacedType:function(){
return (this.ns?this.ns+":"+this.widgetType:this.widgetType).toLowerCase();
},toString:function(){
return "[Widget "+this.getNamespacedType()+", "+(this.widgetId||"NO ID")+"]";
},repr:function(){
return this.toString();
},enable:function(){
this.disabled=false;
},disable:function(){
this.disabled=true;
},onResized:function(){
this.notifyChildrenOfResize();
},notifyChildrenOfResize:function(){
for(var i=0;i<this.children.length;i++){
var _8ab=this.children[i];
if(_8ab.onResized){
_8ab.onResized();
}
}
},create:function(args,_8ad,_8ae,ns){
if(ns){
this.ns=ns;
}
this.satisfyPropertySets(args,_8ad,_8ae);
this.mixInProperties(args,_8ad,_8ae);
this.postMixInProperties(args,_8ad,_8ae);
dojo.widget.manager.add(this);
this.buildRendering(args,_8ad,_8ae);
this.initialize(args,_8ad,_8ae);
this.postInitialize(args,_8ad,_8ae);
this.postCreate(args,_8ad,_8ae);
return this;
},destroy:function(_8b0){
if(this.parent){
this.parent.removeChild(this);
}
this.destroyChildren();
this.uninitialize();
this.destroyRendering(_8b0);
dojo.widget.manager.removeById(this.widgetId);
},destroyChildren:function(){
var _8b1;
var i=0;
while(this.children.length>i){
_8b1=this.children[i];
if(_8b1 instanceof dojo.widget.Widget){
this.removeChild(_8b1);
_8b1.destroy();
continue;
}
i++;
}
},getChildrenOfType:function(type,_8b4){
var ret=[];
var _8b6=dojo.lang.isFunction(type);
if(!_8b6){
type=type.toLowerCase();
}
for(var x=0;x<this.children.length;x++){
if(_8b6){
if(this.children[x] instanceof type){
ret.push(this.children[x]);
}
}else{
if(this.children[x].widgetType.toLowerCase()==type){
ret.push(this.children[x]);
}
}
if(_8b4){
ret=ret.concat(this.children[x].getChildrenOfType(type,_8b4));
}
}
return ret;
},getDescendants:function(){
var _8b8=[];
var _8b9=[this];
var elem;
while((elem=_8b9.pop())){
_8b8.push(elem);
if(elem.children){
dojo.lang.forEach(elem.children,function(elem){
_8b9.push(elem);
});
}
}
return _8b8;
},isFirstChild:function(){
return this===this.parent.children[0];
},isLastChild:function(){
return this===this.parent.children[this.parent.children.length-1];
},satisfyPropertySets:function(args){
return args;
},mixInProperties:function(args,frag){
if((args["fastMixIn"])||(frag["fastMixIn"])){
for(var x in args){
this[x]=args[x];
}
return;
}
var _8c0;
var _8c1=dojo.widget.lcArgsCache[this.widgetType];
if(_8c1==null){
_8c1={};
for(var y in this){
_8c1[((new String(y)).toLowerCase())]=y;
}
dojo.widget.lcArgsCache[this.widgetType]=_8c1;
}
var _8c3={};
for(var x in args){
if(!this[x]){
var y=_8c1[(new String(x)).toLowerCase()];
if(y){
args[y]=args[x];
x=y;
}
}
if(_8c3[x]){
continue;
}
_8c3[x]=true;
if((typeof this[x])!=(typeof _8c0)){
if(typeof args[x]!="string"){
this[x]=args[x];
}else{
if(dojo.lang.isString(this[x])){
this[x]=args[x];
}else{
if(dojo.lang.isNumber(this[x])){
this[x]=new Number(args[x]);
}else{
if(dojo.lang.isBoolean(this[x])){
this[x]=(args[x].toLowerCase()=="false")?false:true;
}else{
if(dojo.lang.isFunction(this[x])){
if(args[x].search(/[^\w\.]+/i)==-1){
this[x]=dojo.evalObjPath(args[x],false);
}else{
var tn=dojo.lang.nameAnonFunc(new Function(args[x]),this);
dojo.event.kwConnect({srcObj:this,srcFunc:x,adviceObj:this,adviceFunc:tn});
}
}else{
if(dojo.lang.isArray(this[x])){
this[x]=args[x].split(";");
}else{
if(this[x] instanceof Date){
this[x]=new Date(Number(args[x]));
}else{
if(typeof this[x]=="object"){
if(this[x] instanceof dojo.uri.Uri){
this[x]=dojo.uri.dojoUri(args[x]);
}else{
var _8c5=args[x].split(";");
for(var y=0;y<_8c5.length;y++){
var si=_8c5[y].indexOf(":");
if((si!=-1)&&(_8c5[y].length>si)){
this[x][_8c5[y].substr(0,si).replace(/^\s+|\s+$/g,"")]=_8c5[y].substr(si+1);
}
}
}
}else{
this[x]=args[x];
}
}
}
}
}
}
}
}
}else{
this.extraArgs[x.toLowerCase()]=args[x];
}
}
},postMixInProperties:function(args,frag,_8c9){
},initialize:function(args,frag,_8cc){
return false;
},postInitialize:function(args,frag,_8cf){
return false;
},postCreate:function(args,frag,_8d2){
return false;
},uninitialize:function(){
return false;
},buildRendering:function(args,frag,_8d5){
dojo.unimplemented("dojo.widget.Widget.buildRendering, on "+this.toString()+", ");
return false;
},destroyRendering:function(){
dojo.unimplemented("dojo.widget.Widget.destroyRendering");
return false;
},addedTo:function(_8d6){
},addChild:function(_8d7){
dojo.unimplemented("dojo.widget.Widget.addChild");
return false;
},removeChild:function(_8d8){
for(var x=0;x<this.children.length;x++){
if(this.children[x]===_8d8){
this.children.splice(x,1);
_8d8.parent=null;
break;
}
}
return _8d8;
},getPreviousSibling:function(){
var idx=this.getParentIndex();
if(idx<=0){
return null;
}
return this.parent.children[idx-1];
},getSiblings:function(){
return this.parent.children;
},getParentIndex:function(){
return dojo.lang.indexOf(this.parent.children,this,true);
},getNextSibling:function(){
var idx=this.getParentIndex();
if(idx==this.parent.children.length-1){
return null;
}
if(idx<0){
return null;
}
return this.parent.children[idx+1];
}});
dojo.widget.lcArgsCache={};
dojo.widget.tags={};
dojo.widget.tags.addParseTreeHandler=function(type){
dojo.deprecated("addParseTreeHandler",". ParseTreeHandlers are now reserved for components. Any unfiltered DojoML tag without a ParseTreeHandler is assumed to be a widget","0.5");
};
dojo.widget.tags["dojo:propertyset"]=function(_8dd,_8de,_8df){
var _8e0=_8de.parseProperties(_8dd["dojo:propertyset"]);
};
dojo.widget.tags["dojo:connect"]=function(_8e1,_8e2,_8e3){
var _8e4=_8e2.parseProperties(_8e1["dojo:connect"]);
};
dojo.widget.buildWidgetFromParseTree=function(type,frag,_8e7,_8e8,_8e9,_8ea){
dojo.a11y.setAccessibleMode();
var _8eb=type.split(":");
_8eb=(_8eb.length==2)?_8eb[1]:type;
var _8ec=_8ea||_8e7.parseProperties(frag[frag["ns"]+":"+_8eb]);
var _8ed=dojo.widget.manager.getImplementation(_8eb,null,null,frag["ns"]);
if(!_8ed){
throw new Error("cannot find \""+type+"\" widget");
}else{
if(!_8ed.create){
throw new Error("\""+type+"\" widget object has no \"create\" method and does not appear to implement *Widget");
}
}
_8ec["dojoinsertionindex"]=_8e9;
var ret=_8ed.create(_8ec,frag,_8e8,frag["ns"]);
return ret;
};
dojo.widget.defineWidget=function(_8ef,_8f0,_8f1,init,_8f3){
if(dojo.lang.isString(arguments[3])){
dojo.widget._defineWidget(arguments[0],arguments[3],arguments[1],arguments[4],arguments[2]);
}else{
var args=[arguments[0]],p=3;
if(dojo.lang.isString(arguments[1])){
args.push(arguments[1],arguments[2]);
}else{
args.push("",arguments[1]);
p=2;
}
if(dojo.lang.isFunction(arguments[p])){
args.push(arguments[p],arguments[p+1]);
}else{
args.push(null,arguments[p]);
}
dojo.widget._defineWidget.apply(this,args);
}
};
dojo.widget.defineWidget.renderers="html|svg|vml";
dojo.widget._defineWidget=function(_8f6,_8f7,_8f8,init,_8fa){
var _8fb=_8f6.split(".");
var type=_8fb.pop();
var regx="\\.("+(_8f7?_8f7+"|":"")+dojo.widget.defineWidget.renderers+")\\.";
var r=_8f6.search(new RegExp(regx));
_8fb=(r<0?_8fb.join("."):_8f6.substr(0,r));
dojo.widget.manager.registerWidgetPackage(_8fb);
var pos=_8fb.indexOf(".");
var _900=(pos>-1)?_8fb.substring(0,pos):_8fb;
_8fa=(_8fa)||{};
_8fa.widgetType=type;
if((!init)&&(_8fa["classConstructor"])){
init=_8fa.classConstructor;
delete _8fa.classConstructor;
}
dojo.declare(_8f6,_8f8,init,_8fa);
};
dojo.provide("dojo.widget.Parse");
dojo.widget.Parse=function(_901){
this.propertySetsList=[];
this.fragment=_901;
this.createComponents=function(frag,_903){
var _904=[];
var _905=false;
try{
if(frag&&frag.tagName&&(frag!=frag.nodeRef)){
var _906=dojo.widget.tags;
var tna=String(frag.tagName).split(";");
for(var x=0;x<tna.length;x++){
var ltn=tna[x].replace(/^\s+|\s+$/g,"").toLowerCase();
frag.tagName=ltn;
var ret;
if(_906[ltn]){
_905=true;
ret=_906[ltn](frag,this,_903,frag.index);
_904.push(ret);
}else{
if(ltn.indexOf(":")==-1){
ltn="dojo:"+ltn;
}
ret=dojo.widget.buildWidgetFromParseTree(ltn,frag,this,_903,frag.index);
if(ret){
_905=true;
_904.push(ret);
}
}
}
}
}
catch(e){
dojo.debug("dojo.widget.Parse: error:",e);
}
if(!_905){
_904=_904.concat(this.createSubComponents(frag,_903));
}
return _904;
};
this.createSubComponents=function(_90b,_90c){
var frag,_90e=[];
for(var item in _90b){
frag=_90b[item];
if(frag&&typeof frag=="object"&&(frag!=_90b.nodeRef)&&(frag!=_90b.tagName)&&(!dojo.dom.isNode(frag))){
_90e=_90e.concat(this.createComponents(frag,_90c));
}
}
return _90e;
};
this.parsePropertySets=function(_910){
return [];
};
this.parseProperties=function(_911){
var _912={};
for(var item in _911){
if((_911[item]==_911.tagName)||(_911[item]==_911.nodeRef)){
}else{
var frag=_911[item];
if(frag.tagName&&dojo.widget.tags[frag.tagName.toLowerCase()]){
}else{
if(frag[0]&&frag[0].value!=""&&frag[0].value!=null){
try{
if(item.toLowerCase()=="dataprovider"){
var _915=this;
this.getDataProvider(_915,frag[0].value);
_912.dataProvider=this.dataProvider;
}
_912[item]=frag[0].value;
var _916=this.parseProperties(frag);
for(var _917 in _916){
_912[_917]=_916[_917];
}
}
catch(e){
dojo.debug(e);
}
}
}
switch(item.toLowerCase()){
case "checked":
case "disabled":
if(typeof _912[item]!="boolean"){
_912[item]=true;
}
break;
}
}
}
return _912;
};
this.getDataProvider=function(_918,_919){
dojo.io.bind({url:_919,load:function(type,_91b){
if(type=="load"){
_918.dataProvider=_91b;
}
},mimetype:"text/javascript",sync:true});
};
this.getPropertySetById=function(_91c){
for(var x=0;x<this.propertySetsList.length;x++){
if(_91c==this.propertySetsList[x]["id"][0].value){
return this.propertySetsList[x];
}
}
return "";
};
this.getPropertySetsByType=function(_91e){
var _91f=[];
for(var x=0;x<this.propertySetsList.length;x++){
var cpl=this.propertySetsList[x];
var cpcc=cpl.componentClass||cpl.componentType||null;
var _923=this.propertySetsList[x]["id"][0].value;
if(cpcc&&(_923==cpcc[0].value)){
_91f.push(cpl);
}
}
return _91f;
};
this.getPropertySets=function(_924){
var ppl="dojo:propertyproviderlist";
var _926=[];
var _927=_924.tagName;
if(_924[ppl]){
var _928=_924[ppl].value.split(" ");
for(var _929 in _928){
if((_929.indexOf("..")==-1)&&(_929.indexOf("://")==-1)){
var _92a=this.getPropertySetById(_929);
if(_92a!=""){
_926.push(_92a);
}
}else{
}
}
}
return this.getPropertySetsByType(_927).concat(_926);
};
this.createComponentFromScript=function(_92b,_92c,_92d,ns){
_92d.fastMixIn=true;
var ltn=(ns||"dojo")+":"+_92c.toLowerCase();
if(dojo.widget.tags[ltn]){
return [dojo.widget.tags[ltn](_92d,this,null,null,_92d)];
}
return [dojo.widget.buildWidgetFromParseTree(ltn,_92d,this,null,null,_92d)];
};
};
dojo.widget._parser_collection={"dojo":new dojo.widget.Parse()};
dojo.widget.getParser=function(name){
if(!name){
name="dojo";
}
if(!this._parser_collection[name]){
this._parser_collection[name]=new dojo.widget.Parse();
}
return this._parser_collection[name];
};
dojo.widget.createWidget=function(name,_932,_933,_934){
var _935=false;
var _936=(typeof name=="string");
if(_936){
var pos=name.indexOf(":");
var ns=(pos>-1)?name.substring(0,pos):"dojo";
if(pos>-1){
name=name.substring(pos+1);
}
var _939=name.toLowerCase();
var _93a=ns+":"+_939;
_935=(dojo.byId(name)&&!dojo.widget.tags[_93a]);
}
if((arguments.length==1)&&(_935||!_936)){
var xp=new dojo.xml.Parse();
var tn=_935?dojo.byId(name):name;
return dojo.widget.getParser().createComponents(xp.parseElement(tn,null,true))[0];
}
function fromScript(_93d,name,_93f,ns){
_93f[_93a]={dojotype:[{value:_939}],nodeRef:_93d,fastMixIn:true};
_93f.ns=ns;
return dojo.widget.getParser().createComponentFromScript(_93d,name,_93f,ns);
}
_932=_932||{};
var _941=false;
var tn=null;
var h=dojo.render.html.capable;
if(h){
tn=document.createElement("span");
}
if(!_933){
_941=true;
_933=tn;
if(h){
dojo.body().appendChild(_933);
}
}else{
if(_934){
dojo.dom.insertAtPosition(tn,_933,_934);
}else{
tn=_933;
}
}
var _943=fromScript(tn,name.toLowerCase(),_932,ns);
if((!_943)||(!_943[0])||(typeof _943[0].widgetType=="undefined")){
throw new Error("createWidget: Creation of \""+name+"\" widget failed.");
}
try{
if(_941&&_943[0].domNode.parentNode){
_943[0].domNode.parentNode.removeChild(_943[0].domNode);
}
}
catch(e){
dojo.debug(e);
}
return _943[0];
};
dojo.provide("dojo.widget.DomWidget");
dojo.widget._cssFiles={};
dojo.widget._cssStrings={};
dojo.widget._templateCache={};
dojo.widget.defaultStrings={dojoRoot:dojo.hostenv.getBaseScriptUri(),baseScriptUri:dojo.hostenv.getBaseScriptUri()};
dojo.widget.fillFromTemplateCache=function(obj,_945,_946,_947){
var _948=_945||obj.templatePath;
var _949=dojo.widget._templateCache;
if(!_948&&!obj["widgetType"]){
do{
var _94a="__dummyTemplate__"+dojo.widget._templateCache.dummyCount++;
}while(_949[_94a]);
obj.widgetType=_94a;
}
var wt=_948?_948.toString():obj.widgetType;
var ts=_949[wt];
if(!ts){
_949[wt]={"string":null,"node":null};
if(_947){
ts={};
}else{
ts=_949[wt];
}
}
if((!obj.templateString)&&(!_947)){
obj.templateString=_946||ts["string"];
}
if((!obj.templateNode)&&(!_947)){
obj.templateNode=ts["node"];
}
if((!obj.templateNode)&&(!obj.templateString)&&(_948)){
var _94d=dojo.hostenv.getText(_948);
if(_94d){
_94d=_94d.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");
var _94e=_94d.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_94e){
_94d=_94e[1];
}
}else{
_94d="";
}
obj.templateString=_94d;
if(!_947){
_949[wt]["string"]=_94d;
}
}
if((!ts["string"])&&(!_947)){
ts.string=obj.templateString;
}
};
dojo.widget._templateCache.dummyCount=0;
dojo.widget.attachProperties=["dojoAttachPoint","id"];
dojo.widget.eventAttachProperty="dojoAttachEvent";
dojo.widget.onBuildProperty="dojoOnBuild";
dojo.widget.waiNames=["waiRole","waiState"];
dojo.widget.wai={waiRole:{name:"waiRole","namespace":"http://www.w3.org/TR/xhtml2",alias:"x2",prefix:"wairole:"},waiState:{name:"waiState","namespace":"http://www.w3.org/2005/07/aaa",alias:"aaa",prefix:""},setAttr:function(node,ns,attr,_952){
if(dojo.render.html.ie){
node.setAttribute(this[ns].alias+":"+attr,this[ns].prefix+_952);
}else{
node.setAttributeNS(this[ns]["namespace"],attr,this[ns].prefix+_952);
}
},getAttr:function(node,ns,attr){
if(dojo.render.html.ie){
return node.getAttribute(this[ns].alias+":"+attr);
}else{
return node.getAttributeNS(this[ns]["namespace"],attr);
}
},removeAttr:function(node,ns,attr){
var _959=true;
if(dojo.render.html.ie){
_959=node.removeAttribute(this[ns].alias+":"+attr);
}else{
node.removeAttributeNS(this[ns]["namespace"],attr);
}
return _959;
}};
dojo.widget.attachTemplateNodes=function(_95a,_95b,_95c){
var _95d=dojo.dom.ELEMENT_NODE;
function trim(str){
return str.replace(/^\s+|\s+$/g,"");
}
if(!_95a){
_95a=_95b.domNode;
}
if(_95a.nodeType!=_95d){
return;
}
var _95f=_95a.all||_95a.getElementsByTagName("*");
var _960=_95b;
for(var x=-1;x<_95f.length;x++){
var _962=(x==-1)?_95a:_95f[x];
var _963=[];
if(!_95b.widgetsInTemplate||!_962.getAttribute("dojoType")){
for(var y=0;y<this.attachProperties.length;y++){
var _965=_962.getAttribute(this.attachProperties[y]);
if(_965){
_963=_965.split(";");
for(var z=0;z<_963.length;z++){
if(dojo.lang.isArray(_95b[_963[z]])){
_95b[_963[z]].push(_962);
}else{
_95b[_963[z]]=_962;
}
}
break;
}
}
var _967=_962.getAttribute(this.eventAttachProperty);
if(_967){
var evts=_967.split(";");
for(var y=0;y<evts.length;y++){
if((!evts[y])||(!evts[y].length)){
continue;
}
var _969=null;
var tevt=trim(evts[y]);
if(evts[y].indexOf(":")>=0){
var _96b=tevt.split(":");
tevt=trim(_96b[0]);
_969=trim(_96b[1]);
}
if(!_969){
_969=tevt;
}
var tf=function(){
var ntf=new String(_969);
return function(evt){
if(_960[ntf]){
_960[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_962,tevt,tf,false,true);
}
}
for(var y=0;y<_95c.length;y++){
var _96f=_962.getAttribute(_95c[y]);
if((_96f)&&(_96f.length)){
var _969=null;
var _970=_95c[y].substr(4);
_969=trim(_96f);
var _971=[_969];
if(_969.indexOf(";")>=0){
_971=dojo.lang.map(_969.split(";"),trim);
}
for(var z=0;z<_971.length;z++){
if(!_971[z].length){
continue;
}
var tf=function(){
var ntf=new String(_971[z]);
return function(evt){
if(_960[ntf]){
_960[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_962,_970,tf,false,true);
}
}
}
}
var _974=_962.getAttribute(this.templateProperty);
if(_974){
_95b[_974]=_962;
}
dojo.lang.forEach(dojo.widget.waiNames,function(name){
var wai=dojo.widget.wai[name];
var val=_962.getAttribute(wai.name);
if(val){
if(val.indexOf("-")==-1){
dojo.widget.wai.setAttr(_962,wai.name,"role",val);
}else{
var _978=val.split("-");
dojo.widget.wai.setAttr(_962,wai.name,_978[0],_978[1]);
}
}
},this);
var _979=_962.getAttribute(this.onBuildProperty);
if(_979){
eval("var node = baseNode; var widget = targetObj; "+_979);
}
}
};
dojo.widget.getDojoEventsFromStr=function(str){
var re=/(dojoOn([a-z]+)(\s?))=/gi;
var evts=str?str.match(re)||[]:[];
var ret=[];
var lem={};
for(var x=0;x<evts.length;x++){
if(evts[x].length<1){
continue;
}
var cm=evts[x].replace(/\s/,"");
cm=(cm.slice(0,cm.length-1));
if(!lem[cm]){
lem[cm]=true;
ret.push(cm);
}
}
return ret;
};
dojo.declare("dojo.widget.DomWidget",dojo.widget.Widget,function(){
if((arguments.length>0)&&(typeof arguments[0]=="object")){
this.create(arguments[0]);
}
},{templateNode:null,templateString:null,templateCssString:null,preventClobber:false,domNode:null,containerNode:null,widgetsInTemplate:false,addChild:function(_981,_982,pos,ref,_985){
if(!this.isContainer){
dojo.debug("dojo.widget.DomWidget.addChild() attempted on non-container widget");
return null;
}else{
if(_985==undefined){
_985=this.children.length;
}
this.addWidgetAsDirectChild(_981,_982,pos,ref,_985);
this.registerChild(_981,_985);
}
return _981;
},addWidgetAsDirectChild:function(_986,_987,pos,ref,_98a){
if((!this.containerNode)&&(!_987)){
this.containerNode=this.domNode;
}
var cn=(_987)?_987:this.containerNode;
if(!pos){
pos="after";
}
if(!ref){
if(!cn){
cn=dojo.body();
}
ref=cn.lastChild;
}
if(!_98a){
_98a=0;
}
_986.domNode.setAttribute("dojoinsertionindex",_98a);
if(!ref){
cn.appendChild(_986.domNode);
}else{
if(pos=="insertAtIndex"){
dojo.dom.insertAtIndex(_986.domNode,ref.parentNode,_98a);
}else{
if((pos=="after")&&(ref===cn.lastChild)){
cn.appendChild(_986.domNode);
}else{
dojo.dom.insertAtPosition(_986.domNode,cn,pos);
}
}
}
},registerChild:function(_98c,_98d){
_98c.dojoInsertionIndex=_98d;
var idx=-1;
for(var i=0;i<this.children.length;i++){
if(this.children[i].dojoInsertionIndex<=_98d){
idx=i;
}
}
this.children.splice(idx+1,0,_98c);
_98c.parent=this;
_98c.addedTo(this,idx+1);
delete dojo.widget.manager.topWidgets[_98c.widgetId];
},removeChild:function(_990){
dojo.dom.removeNode(_990.domNode);
return dojo.widget.DomWidget.superclass.removeChild.call(this,_990);
},getFragNodeRef:function(frag){
if(!frag){
return null;
}
if(!frag[this.getNamespacedType()]){
dojo.raise("Error: no frag for widget type "+this.getNamespacedType()+", id "+this.widgetId+" (maybe a widget has set it's type incorrectly)");
}
return frag[this.getNamespacedType()]["nodeRef"];
},postInitialize:function(args,frag,_994){
var _995=this.getFragNodeRef(frag);
if(_994&&(_994.snarfChildDomOutput||!_995)){
_994.addWidgetAsDirectChild(this,"","insertAtIndex","",args["dojoinsertionindex"],_995);
}else{
if(_995){
if(this.domNode&&(this.domNode!==_995)){
this._sourceNodeRef=dojo.dom.replaceNode(_995,this.domNode);
}
}
}
if(_994){
_994.registerChild(this,args.dojoinsertionindex);
}else{
dojo.widget.manager.topWidgets[this.widgetId]=this;
}
if(this.widgetsInTemplate){
var _996=new dojo.xml.Parse();
var _997;
var _998=this.domNode.getElementsByTagName("*");
for(var i=0;i<_998.length;i++){
if(_998[i].getAttribute("dojoAttachPoint")=="subContainerWidget"){
_997=_998[i];
}
if(_998[i].getAttribute("dojoType")){
_998[i].setAttribute("isSubWidget",true);
}
}
if(this.isContainer&&!this.containerNode){
if(_997){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,_997);
frag["dojoDontFollow"]=true;
}
}else{
dojo.debug("No subContainerWidget node can be found in template file for widget "+this);
}
}
var _99b=_996.parseElement(this.domNode,null,true);
dojo.widget.getParser().createSubComponents(_99b,this);
var _99c=[];
var _99d=[this];
var w;
while((w=_99d.pop())){
for(var i=0;i<w.children.length;i++){
var _99f=w.children[i];
if(_99f._processedSubWidgets||!_99f.extraArgs["issubwidget"]){
continue;
}
_99c.push(_99f);
if(_99f.isContainer){
_99d.push(_99f);
}
}
}
for(var i=0;i<_99c.length;i++){
var _9a0=_99c[i];
if(_9a0._processedSubWidgets){
dojo.debug("This should not happen: widget._processedSubWidgets is already true!");
return;
}
_9a0._processedSubWidgets=true;
if(_9a0.extraArgs["dojoattachevent"]){
var evts=_9a0.extraArgs["dojoattachevent"].split(";");
for(var j=0;j<evts.length;j++){
var _9a3=null;
var tevt=dojo.string.trim(evts[j]);
if(tevt.indexOf(":")>=0){
var _9a5=tevt.split(":");
tevt=dojo.string.trim(_9a5[0]);
_9a3=dojo.string.trim(_9a5[1]);
}
if(!_9a3){
_9a3=tevt;
}
if(dojo.lang.isFunction(_9a0[tevt])){
dojo.event.kwConnect({srcObj:_9a0,srcFunc:tevt,targetObj:this,targetFunc:_9a3});
}else{
alert(tevt+" is not a function in widget "+_9a0);
}
}
}
if(_9a0.extraArgs["dojoattachpoint"]){
this[_9a0.extraArgs["dojoattachpoint"]]=_9a0;
}
}
}
if(this.isContainer&&!frag["dojoDontFollow"]){
dojo.widget.getParser().createSubComponents(frag,this);
}
},buildRendering:function(args,frag){
var ts=dojo.widget._templateCache[this.widgetType];
if(args["templatecsspath"]){
args["templateCssPath"]=args["templatecsspath"];
}
var _9a9=args["templateCssPath"]||this.templateCssPath;
if(_9a9&&!dojo.widget._cssFiles[_9a9.toString()]){
if((!this.templateCssString)&&(_9a9)){
this.templateCssString=dojo.hostenv.getText(_9a9);
this.templateCssPath=null;
}
dojo.widget._cssFiles[_9a9.toString()]=true;
}
if((this["templateCssString"])&&(!dojo.widget._cssStrings[this.templateCssString])){
dojo.html.insertCssText(this.templateCssString,null,_9a9);
dojo.widget._cssStrings[this.templateCssString]=true;
}
if((!this.preventClobber)&&((this.templatePath)||(this.templateNode)||((this["templateString"])&&(this.templateString.length))||((typeof ts!="undefined")&&((ts["string"])||(ts["node"]))))){
this.buildFromTemplate(args,frag);
}else{
this.domNode=this.getFragNodeRef(frag);
}
this.fillInTemplate(args,frag);
},buildFromTemplate:function(args,frag){
var _9ac=false;
if(args["templatepath"]){
args["templatePath"]=args["templatepath"];
}
dojo.widget.fillFromTemplateCache(this,args["templatePath"],null,_9ac);
var ts=dojo.widget._templateCache[this.templatePath?this.templatePath.toString():this.widgetType];
if((ts)&&(!_9ac)){
if(!this.templateString.length){
this.templateString=ts["string"];
}
if(!this.templateNode){
this.templateNode=ts["node"];
}
}
var _9ae=false;
var node=null;
var tstr=this.templateString;
if((!this.templateNode)&&(this.templateString)){
_9ae=this.templateString.match(/\$\{([^\}]+)\}/g);
if(_9ae){
var hash=this.strings||{};
for(var key in dojo.widget.defaultStrings){
if(dojo.lang.isUndefined(hash[key])){
hash[key]=dojo.widget.defaultStrings[key];
}
}
for(var i=0;i<_9ae.length;i++){
var key=_9ae[i];
key=key.substring(2,key.length-1);
var kval=(key.substring(0,5)=="this.")?dojo.lang.getObjPathValue(key.substring(5),this):hash[key];
var _9b5;
if((kval)||(dojo.lang.isString(kval))){
_9b5=new String((dojo.lang.isFunction(kval))?kval.call(this,key,this.templateString):kval);
while(_9b5.indexOf("\"")>-1){
_9b5=_9b5.replace("\"","&quot;");
}
tstr=tstr.replace(_9ae[i],_9b5);
}
}
}else{
this.templateNode=this.createNodesFromText(this.templateString,true)[0];
if(!_9ac){
ts.node=this.templateNode;
}
}
}
if((!this.templateNode)&&(!_9ae)){
dojo.debug("DomWidget.buildFromTemplate: could not create template");
return false;
}else{
if(!_9ae){
node=this.templateNode.cloneNode(true);
if(!node){
return false;
}
}else{
node=this.createNodesFromText(tstr,true)[0];
}
}
this.domNode=node;
this.attachTemplateNodes();
if(this.isContainer&&this.containerNode){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,this.containerNode);
}
}
},attachTemplateNodes:function(_9b7,_9b8){
if(!_9b7){
_9b7=this.domNode;
}
if(!_9b8){
_9b8=this;
}
return dojo.widget.attachTemplateNodes(_9b7,_9b8,dojo.widget.getDojoEventsFromStr(this.templateString));
},fillInTemplate:function(){
},destroyRendering:function(){
try{
dojo.dom.destroyNode(this.domNode);
delete this.domNode;
}
catch(e){
}
if(this._sourceNodeRef){
try{
dojo.dom.destroyNode(this._sourceNodeRef);
}
catch(e){
}
}
},createNodesFromText:function(){
dojo.unimplemented("dojo.widget.DomWidget.createNodesFromText");
}});
dojo.provide("dojo.lfx.toggle");
dojo.lfx.toggle.plain={show:function(node,_9ba,_9bb,_9bc){
dojo.html.show(node);
if(dojo.lang.isFunction(_9bc)){
_9bc();
}
},hide:function(node,_9be,_9bf,_9c0){
dojo.html.hide(node);
if(dojo.lang.isFunction(_9c0)){
_9c0();
}
}};
dojo.lfx.toggle.fade={show:function(node,_9c2,_9c3,_9c4){
dojo.lfx.fadeShow(node,_9c2,_9c3,_9c4).play();
},hide:function(node,_9c6,_9c7,_9c8){
dojo.lfx.fadeHide(node,_9c6,_9c7,_9c8).play();
}};
dojo.lfx.toggle.wipe={show:function(node,_9ca,_9cb,_9cc){
dojo.lfx.wipeIn(node,_9ca,_9cb,_9cc).play();
},hide:function(node,_9ce,_9cf,_9d0){
dojo.lfx.wipeOut(node,_9ce,_9cf,_9d0).play();
}};
dojo.lfx.toggle.explode={show:function(node,_9d2,_9d3,_9d4,_9d5){
dojo.lfx.explode(_9d5||{x:0,y:0,width:0,height:0},node,_9d2,_9d3,_9d4).play();
},hide:function(node,_9d7,_9d8,_9d9,_9da){
dojo.lfx.implode(node,_9da||{x:0,y:0,width:0,height:0},_9d7,_9d8,_9d9).play();
}};
dojo.provide("dojo.widget.HtmlWidget");
dojo.declare("dojo.widget.HtmlWidget",dojo.widget.DomWidget,{templateCssPath:null,templatePath:null,lang:"",toggle:"plain",toggleDuration:150,initialize:function(args,frag){
},postMixInProperties:function(args,frag){
if(this.lang===""){
this.lang=null;
}
this.toggleObj=dojo.lfx.toggle[this.toggle.toLowerCase()]||dojo.lfx.toggle.plain;
},createNodesFromText:function(txt,wrap){
return dojo.html.createNodesFromText(txt,wrap);
},destroyRendering:function(_9e1){
try{
if(this.bgIframe){
this.bgIframe.remove();
delete this.bgIframe;
}
if(!_9e1&&this.domNode){
dojo.event.browser.clean(this.domNode);
}
dojo.widget.HtmlWidget.superclass.destroyRendering.call(this);
}
catch(e){
}
},isShowing:function(){
return dojo.html.isShowing(this.domNode);
},toggleShowing:function(){
if(this.isShowing()){
this.hide();
}else{
this.show();
}
},show:function(){
if(this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.show(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onShow),this.explodeSrc);
},onShow:function(){
this.animationInProgress=false;
this.checkSize();
},hide:function(){
if(!this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.hide(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onHide),this.explodeSrc);
},onHide:function(){
this.animationInProgress=false;
},_isResized:function(w,h){
if(!this.isShowing()){
return false;
}
var wh=dojo.html.getMarginBox(this.domNode);
var _9e5=w||wh.width;
var _9e6=h||wh.height;
if(this.width==_9e5&&this.height==_9e6){
return false;
}
this.width=_9e5;
this.height=_9e6;
return true;
},checkSize:function(){
if(!this._isResized()){
return;
}
this.onResized();
},resizeTo:function(w,h){
dojo.html.setMarginBox(this.domNode,{width:w,height:h});
if(this.isShowing()){
this.onResized();
}
},resizeSoon:function(){
if(this.isShowing()){
dojo.lang.setTimeout(this,this.onResized,0);
}
},onResized:function(){
dojo.lang.forEach(this.children,function(_9e9){
if(_9e9.checkSize){
_9e9.checkSize();
}
});
}});
dojo.provide("dojo.widget.*");
dojo.provide("dojo.io.*");
dojo.provide("dojo.widget.ContentPane");
dojo.widget.defineWidget("dojo.widget.ContentPane",dojo.widget.HtmlWidget,function(){
this._styleNodes=[];
this._onLoadStack=[];
this._onUnloadStack=[];
this._callOnUnload=false;
this._ioBindObj;
this.scriptScope;
this.bindArgs={};
},{isContainer:true,adjustPaths:true,href:"",extractContent:true,parseContent:true,cacheContent:true,preload:false,refreshOnShow:false,handler:"",executeScripts:false,scriptSeparation:true,loadingMessage:"Loading...",isLoaded:false,postCreate:function(args,frag,_9ec){
if(this.handler!==""){
this.setHandler(this.handler);
}
if(this.isShowing()||this.preload){
this.loadContents();
}
},show:function(){
if(this.refreshOnShow){
this.refresh();
}else{
this.loadContents();
}
dojo.widget.ContentPane.superclass.show.call(this);
},refresh:function(){
this.isLoaded=false;
this.loadContents();
},loadContents:function(){
if(this.isLoaded){
return;
}
if(dojo.lang.isFunction(this.handler)){
this._runHandler();
}else{
if(this.href!=""){
this._downloadExternalContent(this.href,this.cacheContent&&!this.refreshOnShow);
}
}
},setUrl:function(url){
this.href=url;
this.isLoaded=false;
if(this.preload||this.isShowing()){
this.loadContents();
}
},abort:function(){
var bind=this._ioBindObj;
if(!bind||!bind.abort){
return;
}
bind.abort();
delete this._ioBindObj;
},_downloadExternalContent:function(url,_9f0){
this.abort();
this._handleDefaults(this.loadingMessage,"onDownloadStart");
var self=this;
this._ioBindObj=dojo.io.bind(this._cacheSetting({url:url,mimetype:"text/html",handler:function(type,data,xhr){
delete self._ioBindObj;
if(type=="load"){
self.onDownloadEnd.call(self,url,data);
}else{
var e={responseText:xhr.responseText,status:xhr.status,statusText:xhr.statusText,responseHeaders:xhr.getAllResponseHeaders(),text:"Error loading '"+url+"' ("+xhr.status+" "+xhr.statusText+")"};
self._handleDefaults.call(self,e,"onDownloadError");
self.onLoad();
}
}},_9f0));
},_cacheSetting:function(_9f6,_9f7){
for(var x in this.bindArgs){
if(dojo.lang.isUndefined(_9f6[x])){
_9f6[x]=this.bindArgs[x];
}
}
if(dojo.lang.isUndefined(_9f6.useCache)){
_9f6.useCache=_9f7;
}
if(dojo.lang.isUndefined(_9f6.preventCache)){
_9f6.preventCache=!_9f7;
}
if(dojo.lang.isUndefined(_9f6.mimetype)){
_9f6.mimetype="text/html";
}
return _9f6;
},onLoad:function(e){
this._runStack("_onLoadStack");
this.isLoaded=true;
},onUnLoad:function(e){
dojo.deprecated(this.widgetType+".onUnLoad, use .onUnload (lowercased load)",0.5);
},onUnload:function(e){
this._runStack("_onUnloadStack");
delete this.scriptScope;
if(this.onUnLoad!==dojo.widget.ContentPane.prototype.onUnLoad){
this.onUnLoad.apply(this,arguments);
}
},_runStack:function(_9fc){
var st=this[_9fc];
var err="";
var _9ff=this.scriptScope||window;
for(var i=0;i<st.length;i++){
try{
st[i].call(_9ff);
}
catch(e){
err+="\n"+st[i]+" failed: "+e.description;
}
}
this[_9fc]=[];
if(err.length){
var name=(_9fc=="_onLoadStack")?"addOnLoad":"addOnUnLoad";
this._handleDefaults(name+" failure\n "+err,"onExecError","debug");
}
},addOnLoad:function(obj,func){
this._pushOnStack(this._onLoadStack,obj,func);
},addOnUnload:function(obj,func){
this._pushOnStack(this._onUnloadStack,obj,func);
},addOnUnLoad:function(){
dojo.deprecated(this.widgetType+".addOnUnLoad, use addOnUnload instead. (lowercased Load)",0.5);
this.addOnUnload.apply(this,arguments);
},_pushOnStack:function(_a06,obj,func){
if(typeof func=="undefined"){
_a06.push(obj);
}else{
_a06.push(function(){
obj[func]();
});
}
},destroy:function(){
this.onUnload();
dojo.widget.ContentPane.superclass.destroy.call(this);
},onExecError:function(e){
},onContentError:function(e){
},onDownloadError:function(e){
},onDownloadStart:function(e){
},onDownloadEnd:function(url,data){
data=this.splitAndFixPaths(data,url);
this.setContent(data);
},_handleDefaults:function(e,_a10,_a11){
if(!_a10){
_a10="onContentError";
}
if(dojo.lang.isString(e)){
e={text:e};
}
if(!e.text){
e.text=e.toString();
}
e.toString=function(){
return this.text;
};
if(typeof e.returnValue!="boolean"){
e.returnValue=true;
}
if(typeof e.preventDefault!="function"){
e.preventDefault=function(){
this.returnValue=false;
};
}
this[_a10](e);
if(e.returnValue){
switch(_a11){
case true:
case "alert":
alert(e.toString());
break;
case "debug":
dojo.debug(e.toString());
break;
default:
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=false;
if(arguments.callee._loopStop){
dojo.debug(e.toString());
}else{
arguments.callee._loopStop=true;
this._setContent(e.toString());
}
}
}
arguments.callee._loopStop=false;
},splitAndFixPaths:function(s,url){
var _a14=[],_a15=[],tmp=[];
var _a17=[],_a18=[],attr=[],_a1a=[];
var str="",path="",fix="",_a1e="",tag="",_a20="";
if(!url){
url="./";
}
if(s){
var _a21=/<title[^>]*>([\s\S]*?)<\/title>/i;
while(_a17=_a21.exec(s)){
_a14.push(_a17[1]);
s=s.substring(0,_a17.index)+s.substr(_a17.index+_a17[0].length);
}
if(this.adjustPaths){
var _a22=/<[a-z][a-z0-9]*[^>]*\s(?:(?:src|href|style)=[^>])+[^>]*>/i;
var _a23=/\s(src|href|style)=(['"]?)([\w()\[\]\/.,\\'"-:;#=&?\s@]+?)\2/i;
var _a24=/^(?:[#]|(?:(?:https?|ftps?|file|javascript|mailto|news):))/;
while(tag=_a22.exec(s)){
str+=s.substring(0,tag.index);
s=s.substring((tag.index+tag[0].length),s.length);
tag=tag[0];
_a1e="";
while(attr=_a23.exec(tag)){
path="";
_a20=attr[3];
switch(attr[1].toLowerCase()){
case "src":
case "href":
if(_a24.exec(_a20)){
path=_a20;
}else{
path=(new dojo.uri.Uri(url,_a20).toString());
}
break;
case "style":
path=dojo.html.fixPathsInCssText(_a20,url);
break;
default:
path=_a20;
}
fix=" "+attr[1]+"="+attr[2]+path+attr[2];
_a1e+=tag.substring(0,attr.index)+fix;
tag=tag.substring((attr.index+attr[0].length),tag.length);
}
str+=_a1e+tag;
}
s=str+s;
}
_a21=/(?:<(style)[^>]*>([\s\S]*?)<\/style>|<link ([^>]*rel=['"]?stylesheet['"]?[^>]*)>)/i;
while(_a17=_a21.exec(s)){
if(_a17[1]&&_a17[1].toLowerCase()=="style"){
_a1a.push(dojo.html.fixPathsInCssText(_a17[2],url));
}else{
if(attr=_a17[3].match(/href=(['"]?)([^'">]*)\1/i)){
_a1a.push({path:attr[2]});
}
}
s=s.substring(0,_a17.index)+s.substr(_a17.index+_a17[0].length);
}
var _a21=/<script([^>]*)>([\s\S]*?)<\/script>/i;
var _a25=/src=(['"]?)([^"']*)\1/i;
var _a26=/.*(\bdojo\b\.js(?:\.uncompressed\.js)?)$/;
var _a27=/(?:var )?\bdjConfig\b(?:[\s]*=[\s]*\{[^}]+\}|\.[\w]*[\s]*=[\s]*[^;\n]*)?;?|dojo\.hostenv\.writeIncludes\(\s*\);?/g;
var _a28=/dojo\.(?:(?:require(?:After)?(?:If)?)|(?:widget\.(?:manager\.)?registerWidgetPackage)|(?:(?:hostenv\.)?setModulePrefix|registerModulePath)|defineNamespace)\((['"]).*?\1\)\s*;?/;
while(_a17=_a21.exec(s)){
if(this.executeScripts&&_a17[1]){
if(attr=_a25.exec(_a17[1])){
if(_a26.exec(attr[2])){
dojo.debug("Security note! inhibit:"+attr[2]+" from  being loaded again.");
}else{
_a15.push({path:attr[2]});
}
}
}
if(_a17[2]){
var sc=_a17[2].replace(_a27,"");
if(!sc){
continue;
}
while(tmp=_a28.exec(sc)){
_a18.push(tmp[0]);
sc=sc.substring(0,tmp.index)+sc.substr(tmp.index+tmp[0].length);
}
if(this.executeScripts){
_a15.push(sc);
}
}
s=s.substr(0,_a17.index)+s.substr(_a17.index+_a17[0].length);
}
if(this.extractContent){
_a17=s.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_a17){
s=_a17[1];
}
}
if(this.executeScripts&&this.scriptSeparation){
var _a21=/(<[a-zA-Z][a-zA-Z0-9]*\s[^>]*?\S=)((['"])[^>]*scriptScope[^>]*>)/;
var _a2a=/([\s'";:\(])scriptScope(.*)/;
str="";
while(tag=_a21.exec(s)){
tmp=((tag[3]=="'")?"\"":"'");
fix="";
str+=s.substring(0,tag.index)+tag[1];
while(attr=_a2a.exec(tag[2])){
tag[2]=tag[2].substring(0,attr.index)+attr[1]+"dojo.widget.byId("+tmp+this.widgetId+tmp+").scriptScope"+attr[2];
}
str+=tag[2];
s=s.substr(tag.index+tag[0].length);
}
s=str+s;
}
}
return {"xml":s,"styles":_a1a,"titles":_a14,"requires":_a18,"scripts":_a15,"url":url};
},_setContent:function(cont){
this.destroyChildren();
for(var i=0;i<this._styleNodes.length;i++){
if(this._styleNodes[i]&&this._styleNodes[i].parentNode){
this._styleNodes[i].parentNode.removeChild(this._styleNodes[i]);
}
}
this._styleNodes=[];
try{
var node=this.containerNode||this.domNode;
while(node.firstChild){
dojo.html.destroyNode(node.firstChild);
}
if(typeof cont!="string"){
node.appendChild(cont);
}else{
node.innerHTML=cont;
}
}
catch(e){
e.text="Couldn't load content:"+e.description;
this._handleDefaults(e,"onContentError");
}
},setContent:function(data){
this.abort();
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=true;
if(!data||dojo.html.isNode(data)){
this._setContent(data);
this.onResized();
this.onLoad();
}else{
if(typeof data.xml!="string"){
this.href="";
data=this.splitAndFixPaths(data);
}
this._setContent(data.xml);
for(var i=0;i<data.styles.length;i++){
if(data.styles[i].path){
this._styleNodes.push(dojo.html.insertCssFile(data.styles[i].path,dojo.doc(),false,true));
}else{
this._styleNodes.push(dojo.html.insertCssText(data.styles[i]));
}
}
if(this.parseContent){
for(var i=0;i<data.requires.length;i++){
try{
eval(data.requires[i]);
}
catch(e){
e.text="ContentPane: error in package loading calls, "+(e.description||e);
this._handleDefaults(e,"onContentError","debug");
}
}
}
var _a30=this;
function asyncParse(){
if(_a30.executeScripts){
_a30._executeScripts(data.scripts);
}
if(_a30.parseContent){
var node=_a30.containerNode||_a30.domNode;
var _a32=new dojo.xml.Parse();
var frag=_a32.parseElement(node,null,true);
dojo.widget.getParser().createSubComponents(frag,_a30);
}
_a30.onResized();
_a30.onLoad();
}
if(dojo.hostenv.isXDomain&&data.requires.length){
dojo.addOnLoad(asyncParse);
}else{
asyncParse();
}
}
},setHandler:function(_a34){
var fcn=dojo.lang.isFunction(_a34)?_a34:window[_a34];
if(!dojo.lang.isFunction(fcn)){
this._handleDefaults("Unable to set handler, '"+_a34+"' not a function.","onExecError",true);
return;
}
this.handler=function(){
return fcn.apply(this,arguments);
};
},_runHandler:function(){
var ret=true;
if(dojo.lang.isFunction(this.handler)){
this.handler(this,this.domNode);
ret=false;
}
this.onLoad();
return ret;
},_executeScripts:function(_a37){
var self=this;
var tmp="",code="";
for(var i=0;i<_a37.length;i++){
if(_a37[i].path){
dojo.io.bind(this._cacheSetting({"url":_a37[i].path,"load":function(type,_a3d){
dojo.lang.hitch(self,tmp=";"+_a3d);
},"error":function(type,_a3f){
_a3f.text=type+" downloading remote script";
self._handleDefaults.call(self,_a3f,"onExecError","debug");
},"mimetype":"text/plain","sync":true},this.cacheContent));
code+=tmp;
}else{
code+=_a37[i];
}
}
try{
if(this.scriptSeparation){
delete this.scriptScope;
this.scriptScope=new (new Function("_container_",code+"; return this;"))(self);
}else{
var djg=dojo.global();
if(djg.execScript){
djg.execScript(code);
}else{
var djd=dojo.doc();
var sc=djd.createElement("script");
sc.appendChild(djd.createTextNode(code));
(this.containerNode||this.domNode).appendChild(sc);
}
}
}
catch(e){
e.text="Error running scripts from content:\n"+e.description;
this._handleDefaults(e,"onExecError","debug");
}
}});
dojo.provide("dojo.widget.html.layout");
dojo.widget.html.layout=function(_a43,_a44,_a45){
dojo.html.addClass(_a43,"dojoLayoutContainer");
_a44=dojo.lang.filter(_a44,function(_a46,idx){
_a46.idx=idx;
return dojo.lang.inArray(["top","bottom","left","right","client","flood"],_a46.layoutAlign);
});
if(_a45&&_a45!="none"){
var rank=function(_a49){
switch(_a49.layoutAlign){
case "flood":
return 1;
case "left":
case "right":
return (_a45=="left-right")?2:3;
case "top":
case "bottom":
return (_a45=="left-right")?3:2;
default:
return 4;
}
};
_a44.sort(function(a,b){
return (rank(a)-rank(b))||(a.idx-b.idx);
});
}
var f={top:dojo.html.getPixelValue(_a43,"padding-top",true),left:dojo.html.getPixelValue(_a43,"padding-left",true)};
dojo.lang.mixin(f,dojo.html.getContentBox(_a43));
dojo.lang.forEach(_a44,function(_a4d){
var elm=_a4d.domNode;
var pos=_a4d.layoutAlign;
with(elm.style){
left=f.left+"px";
top=f.top+"px";
bottom="auto";
right="auto";
}
dojo.html.addClass(elm,"dojoAlign"+dojo.string.capitalize(pos));
if((pos=="top")||(pos=="bottom")){
dojo.html.setMarginBox(elm,{width:f.width});
var h=dojo.html.getMarginBox(elm).height;
f.height-=h;
if(pos=="top"){
f.top+=h;
}else{
elm.style.top=f.top+f.height+"px";
}
if(_a4d.onResized){
_a4d.onResized();
}
}else{
if(pos=="left"||pos=="right"){
var w=dojo.html.getMarginBox(elm).width;
if(_a4d.resizeTo){
_a4d.resizeTo(w,f.height);
}else{
dojo.html.setMarginBox(elm,{width:w,height:f.height});
}
f.width-=w;
if(pos=="left"){
f.left+=w;
}else{
elm.style.left=f.left+f.width+"px";
}
}else{
if(pos=="flood"||pos=="client"){
if(_a4d.resizeTo){
_a4d.resizeTo(f.width,f.height);
}else{
dojo.html.setMarginBox(elm,{width:f.width,height:f.height});
}
}
}
}
});
};
dojo.html.insertCssText(".dojoLayoutContainer{ position: relative; display: block; overflow: hidden; }\n"+"body .dojoAlignTop, body .dojoAlignBottom, body .dojoAlignLeft, body .dojoAlignRight { position: absolute; overflow: hidden; }\n"+"body .dojoAlignClient { position: absolute }\n"+".dojoAlignClient { overflow: auto; }\n");
dojo.provide("dojo.widget.LayoutContainer");
dojo.widget.defineWidget("dojo.widget.LayoutContainer",dojo.widget.HtmlWidget,{isContainer:true,layoutChildPriority:"top-bottom",postCreate:function(){
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},addChild:function(_a52,_a53,pos,ref,_a56){
dojo.widget.LayoutContainer.superclass.addChild.call(this,_a52,_a53,pos,ref,_a56);
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},removeChild:function(pane){
dojo.widget.LayoutContainer.superclass.removeChild.call(this,pane);
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},onResized:function(){
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},show:function(){
this.domNode.style.display="";
this.checkSize();
this.domNode.style.display="none";
this.domNode.style.visibility="";
dojo.widget.LayoutContainer.superclass.show.call(this);
}});
dojo.lang.extend(dojo.widget.Widget,{layoutAlign:"none"});
dojo.provide("dojo.widget.Dialog");
dojo.declare("dojo.widget.ModalDialogBase",null,{isContainer:true,focusElement:"",bgColor:"black",bgOpacity:0.4,followScroll:true,closeOnBackgroundClick:false,trapTabs:function(e){
if(e.target==this.tabStartOuter){
if(this._fromTrap){
this.tabStart.focus();
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabEnd.focus();
}
}else{
if(e.target==this.tabStart){
if(this._fromTrap){
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabEnd.focus();
}
}else{
if(e.target==this.tabEndOuter){
if(this._fromTrap){
this.tabEnd.focus();
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabStart.focus();
}
}else{
if(e.target==this.tabEnd){
if(this._fromTrap){
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabStart.focus();
}
}
}
}
}
},clearTrap:function(e){
var _a5a=this;
setTimeout(function(){
_a5a._fromTrap=false;
},100);
},postCreate:function(){
with(this.domNode.style){
position="absolute";
zIndex=999;
display="none";
overflow="visible";
}
var b=dojo.body();
b.appendChild(this.domNode);
this.bg=document.createElement("div");
this.bg.className="dialogUnderlay";
with(this.bg.style){
position="absolute";
left=top="0px";
zIndex=998;
display="none";
}
b.appendChild(this.bg);
this.setBackgroundColor(this.bgColor);
this.bgIframe=new dojo.html.BackgroundIframe();
if(this.bgIframe.iframe){
with(this.bgIframe.iframe.style){
position="absolute";
left=top="0px";
zIndex=90;
display="none";
}
}
if(this.closeOnBackgroundClick){
dojo.event.kwConnect({srcObj:this.bg,srcFunc:"onclick",adviceObj:this,adviceFunc:"onBackgroundClick",once:true});
}
},uninitialize:function(){
this.bgIframe.remove();
dojo.html.removeNode(this.bg,true);
},setBackgroundColor:function(_a5c){
if(arguments.length>=3){
_a5c=new dojo.gfx.color.Color(arguments[0],arguments[1],arguments[2]);
}else{
_a5c=new dojo.gfx.color.Color(_a5c);
}
this.bg.style.backgroundColor=_a5c.toString();
return this.bgColor=_a5c;
},setBackgroundOpacity:function(op){
if(arguments.length==0){
op=this.bgOpacity;
}
dojo.html.setOpacity(this.bg,op);
try{
this.bgOpacity=dojo.html.getOpacity(this.bg);
}
catch(e){
this.bgOpacity=op;
}
return this.bgOpacity;
},_sizeBackground:function(){
if(this.bgOpacity>0){
var _a5e=dojo.html.getViewport();
var h=_a5e.height;
var w=_a5e.width;
with(this.bg.style){
width=w+"px";
height=h+"px";
}
var _a61=dojo.html.getScroll().offset;
this.bg.style.top=_a61.y+"px";
this.bg.style.left=_a61.x+"px";
var _a5e=dojo.html.getViewport();
if(_a5e.width!=w){
this.bg.style.width=_a5e.width+"px";
}
if(_a5e.height!=h){
this.bg.style.height=_a5e.height+"px";
}
}
this.bgIframe.size(this.bg);
},_showBackground:function(){
if(this.bgOpacity>0){
this.bg.style.display="block";
}
if(this.bgIframe.iframe){
this.bgIframe.iframe.style.display="block";
}
},placeModalDialog:function(){
var _a62=dojo.html.getScroll().offset;
var _a63=dojo.html.getViewport();
var mb;
if(this.isShowing()){
mb=dojo.html.getMarginBox(this.domNode);
}else{
dojo.html.setVisibility(this.domNode,false);
dojo.html.show(this.domNode);
mb=dojo.html.getMarginBox(this.domNode);
dojo.html.hide(this.domNode);
dojo.html.setVisibility(this.domNode,true);
}
var x=_a62.x+(_a63.width-mb.width)/2;
var y=_a62.y+(_a63.height-mb.height)/2;
with(this.domNode.style){
left=x+"px";
top=y+"px";
}
},_onKey:function(evt){
if(evt.key){
var node=evt.target;
while(node!=null){
if(node==this.domNode){
return;
}
node=node.parentNode;
}
if(evt.key!=evt.KEY_TAB){
dojo.event.browser.stopEvent(evt);
}else{
if(!dojo.render.html.opera){
try{
this.tabStart.focus();
}
catch(e){
}
}
}
}
},showModalDialog:function(){
if(this.followScroll&&!this._scrollConnected){
this._scrollConnected=true;
dojo.event.connect(window,"onscroll",this,"_onScroll");
}
dojo.event.connect(document.documentElement,"onkey",this,"_onKey");
this.placeModalDialog();
this.setBackgroundOpacity();
this._sizeBackground();
this._showBackground();
this._fromTrap=true;
setTimeout(dojo.lang.hitch(this,function(){
try{
this.tabStart.focus();
}
catch(e){
}
}),50);
},hideModalDialog:function(){
if(this.focusElement){
dojo.byId(this.focusElement).focus();
dojo.byId(this.focusElement).blur();
}
this.bg.style.display="none";
this.bg.style.width=this.bg.style.height="1px";
if(this.bgIframe.iframe){
this.bgIframe.iframe.style.display="none";
}
dojo.event.disconnect(document.documentElement,"onkey",this,"_onKey");
if(this._scrollConnected){
this._scrollConnected=false;
dojo.event.disconnect(window,"onscroll",this,"_onScroll");
}
},_onScroll:function(){
var _a69=dojo.html.getScroll().offset;
this.bg.style.top=_a69.y+"px";
this.bg.style.left=_a69.x+"px";
this.placeModalDialog();
},checkSize:function(){
if(this.isShowing()){
this._sizeBackground();
this.placeModalDialog();
this.onResized();
}
},onBackgroundClick:function(){
if(this.lifetime-this.timeRemaining>=this.blockDuration){
return;
}
this.hide();
}});
dojo.widget.defineWidget("dojo.widget.Dialog",[dojo.widget.ContentPane,dojo.widget.ModalDialogBase],{templatePath:dojo.uri.dojoUri("src/widget/templates/Dialog.html"),blockDuration:0,lifetime:0,closeNode:"",postMixInProperties:function(){
dojo.widget.Dialog.superclass.postMixInProperties.apply(this,arguments);
if(this.closeNode){
this.setCloseControl(this.closeNode);
}
},postCreate:function(){
dojo.widget.Dialog.superclass.postCreate.apply(this,arguments);
dojo.widget.ModalDialogBase.prototype.postCreate.apply(this,arguments);
},show:function(){
if(this.lifetime){
this.timeRemaining=this.lifetime;
if(this.timerNode){
this.timerNode.innerHTML=Math.ceil(this.timeRemaining/1000);
}
if(this.blockDuration&&this.closeNode){
if(this.lifetime>this.blockDuration){
this.closeNode.style.visibility="hidden";
}else{
this.closeNode.style.display="none";
}
}
if(this.timer){
clearInterval(this.timer);
}
this.timer=setInterval(dojo.lang.hitch(this,"_onTick"),100);
}
this.showModalDialog();
dojo.widget.Dialog.superclass.show.call(this);
},onLoad:function(){
this.placeModalDialog();
dojo.widget.Dialog.superclass.onLoad.call(this);
},fillInTemplate:function(){
},hide:function(){
this.hideModalDialog();
dojo.widget.Dialog.superclass.hide.call(this);
if(this.timer){
clearInterval(this.timer);
}
},setTimerNode:function(node){
this.timerNode=node;
},setCloseControl:function(node){
this.closeNode=dojo.byId(node);
dojo.event.connect(this.closeNode,"onclick",this,"hide");
},setShowControl:function(node){
node=dojo.byId(node);
dojo.event.connect(node,"onclick",this,"show");
},_onTick:function(){
if(this.timer){
this.timeRemaining-=100;
if(this.lifetime-this.timeRemaining>=this.blockDuration){
if(this.closeNode){
this.closeNode.style.visibility="visible";
}
}
if(!this.timeRemaining){
clearInterval(this.timer);
this.hide();
}else{
if(this.timerNode){
this.timerNode.innerHTML=Math.ceil(this.timeRemaining/1000);
}
}
}
}});

