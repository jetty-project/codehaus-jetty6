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
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 6392 $".match(/[0-9]+/)[0]),toString:function(){
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
return _6e?_6e.toLowerCase():dojo.locale;
};
dojo.hostenv.searchLocalePath=function(_6f,_70,_71){
_6f=dojo.hostenv.normalizeLocale(_6f);
var _72=_6f.split("-");
var _73=[];
for(var i=_72.length;i>0;i--){
_73.push(_72.slice(0,i).join("-"));
}
_73.push(false);
if(_70){
_73.reverse();
}
for(var j=_73.length-1;j>=0;j--){
var loc=_73[j]||"ROOT";
var _77=_71(loc);
if(_77){
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
function preload(_78){
_78=dojo.hostenv.normalizeLocale(_78);
dojo.hostenv.searchLocalePath(_78,true,function(loc){
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
var _7b=djConfig.extraLocale||[];
for(var i=0;i<_7b.length;i++){
preload(_7b[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7d,_7e,_7f){
dojo.hostenv.preloadLocalizations();
var _80=[_7d,"nls",_7e].join(".");
var _81=dojo.hostenv.findModule(_80);
if(_81){
if(djConfig.localizationComplete&&_81._built){
return;
}
var _82=dojo.hostenv.normalizeLocale(_7f).replace("-","_");
var _83=_80+"."+_82;
if(dojo.hostenv.findModule(_83)){
return;
}
}
_81=dojo.hostenv.startPackage(_80);
var _84=dojo.hostenv.getModuleSymbols(_7d);
var _85=_84.concat("nls").join("/");
var _86;
dojo.hostenv.searchLocalePath(_7f,false,function(loc){
var _88=loc.replace("-","_");
var _89=_80+"."+_88;
var _8a=false;
if(!dojo.hostenv.findModule(_89)){
dojo.hostenv.startPackage(_89);
var _8b=[_85];
if(loc!="ROOT"){
_8b.push(loc);
}
_8b.push(_7e);
var _8c=_8b.join("/")+".js";
_8a=dojo.hostenv.loadPath(_8c,null,function(_8d){
var _8e=function(){
};
_8e.prototype=_86;
_81[_88]=new _8e();
for(var j in _8d){
_81[_88][j]=_8d[j];
}
});
}else{
_8a=true;
}
if(_8a&&_81[_88]){
_86=_81[_88];
}else{
_81[_88]=_86;
}
});
};
(function(){
var _90=djConfig.extraLocale;
if(_90){
if(!_90 instanceof Array){
_90=[_90];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_94){
req(m,b,_94);
if(_94){
return;
}
for(var i=0;i<_90.length;i++){
req(m,b,_90[i]);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _96=document.location.toString();
var _97=_96.split("?",2);
if(_97.length>1){
var _98=_97[1];
var _99=_98.split("&");
for(var x in _99){
var sp=_99[x].split("=");
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
var _9d=document.getElementsByTagName("script");
var _9e=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_9d.length;i++){
var src=_9d[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_9e);
if(m){
var _a2=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_a2+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_a2;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_a2;
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
var _aa=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_aa>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_aa+6,_aa+14);
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
var _ac=window["document"];
var tdi=_ac["implementation"];
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
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b0=null;
var _b1=null;
try{
_b0=new XMLHttpRequest();
}
catch(e){
}
if(!_b0){
for(var i=0;i<3;++i){
var _b3=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b0=new ActiveXObject(_b3);
}
catch(e){
_b1=e;
}
if(_b0){
dojo.hostenv._XMLHTTP_PROGIDS=[_b3];
break;
}
}
}
if(!_b0){
return dojo.raise("XMLHTTP not available",_b1);
}
return _b0;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_b5,_b6){
if(!_b5){
this._blockAsync=true;
}
var _b7=this.getXmlhttpObject();
function isDocumentOk(_b8){
var _b9=_b8["status"];
return Boolean((!_b9)||((200<=_b9)&&(300>_b9))||(_b9==304));
}
if(_b5){
var _ba=this,_bb=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_b7.onreadystatechange=function(){
if(_bb){
gbl.clearTimeout(_bb);
_bb=null;
}
if(_ba._blockAsync||(xhr&&xhr._blockAsync)){
_bb=gbl.setTimeout(function(){
_b7.onreadystatechange.apply(this);
},10);
}else{
if(4==_b7.readyState){
if(isDocumentOk(_b7)){
_b5(_b7.responseText);
}
}
}
};
}
_b7.open("GET",uri,_b5?true:false);
try{
_b7.send(null);
if(_b5){
return null;
}
if(!isDocumentOk(_b7)){
var err=Error("Unable to load "+uri+" status:"+_b7.status);
err.status=_b7.status;
err.responseText=_b7.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_b6)&&(!_b5)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _b7.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_bf){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_bf);
}else{
try{
var _c0=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c0){
_c0=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_bf));
_c0.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_bf+"</div>");
}
catch(e2){
window.status=_bf;
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
function dj_addNodeEvtHdlr(_c2,_c3,fp,_c5){
var _c6=_c2["on"+_c3]||function(){
};
_c2["on"+_c3]=function(){
fp.apply(_c2,arguments);
_c6.apply(_c2,arguments);
};
return true;
}
function dj_load_init(e){
var _c8=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_c8!="domcontentloaded"&&_c8!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _c9=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_c9();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_c9);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&!djConfig.delayMozLoadingFix)){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.attachEvent("onreadystatechange",function(e){
if(document.readyState=="complete"){
dj_load_init();
}
});
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
var _cb=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_cb=_cb.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_cb=_cb.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_cb.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _cc=new dojo.xml.Parse();
if(_cb.length>0){
for(var x=0;x<_cb.length;x++){
var _ce=document.getElementById(_cb[x]);
if(!_ce){
continue;
}
var _cf=_cc.parseElement(_ce,null,true);
dojo.widget.getParser().createComponents(_cf);
}
}else{
if(djConfig.parseWidgets){
var _cf=_cc.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_cf);
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
dojo.setContext=function(_d4,_d5){
dj_currentContext=_d4;
dj_currentDocument=_d5;
};
dojo._fireCallback=function(_d6,_d7,_d8){
if((_d7)&&((typeof _d6=="string")||(_d6 instanceof String))){
_d6=_d7[_d6];
}
return (_d7?_d6.apply(_d7,_d8||[]):_d6());
};
dojo.withGlobal=function(_d9,_da,_db,_dc){
var _dd;
var _de=dj_currentContext;
var _df=dj_currentDocument;
try{
dojo.setContext(_d9,_d9.document);
_dd=dojo._fireCallback(_da,_db,_dc);
}
finally{
dojo.setContext(_de,_df);
}
return _dd;
};
dojo.withDoc=function(_e0,_e1,_e2,_e3){
var _e4;
var _e5=dj_currentDocument;
try{
dj_currentDocument=_e0;
_e4=dojo._fireCallback(_e1,_e2,_e3);
}
finally{
dj_currentDocument=_e5;
}
return _e4;
};
}
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _e6=false;
var _e7=false;
var _e8=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_e6=true;
}else{
if(typeof this["load"]=="function"){
_e7=true;
}else{
if(window.widget){
_e8=true;
}
}
}
var _e9=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_e9.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_e6)&&(!_e8)){
_e9.push("browser_debug.js");
}
var _ea=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_ea=djConfig["baseLoaderUri"];
}
for(var x=0;x<_e9.length;x++){
var _ec=_ea+"src/"+_e9[x];
if(_e6||_e7){
load(_ec);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_ec+"'></scr"+"ipt>");
}
catch(e){
var _ed=document.createElement("script");
_ed.src=_ec;
document.getElementsByTagName("head")[0].appendChild(_ed);
}
}
}
})();
dojo.debug=function(){
if(!djConfig.isDebug){
return;
}
var _ee=arguments;
if(dj_undef("println",dojo.hostenv)){
dojo.raise("dojo.debug not available (yet?)");
}
var _ef=dj_global["jum"]&&!dj_global["jum"].isBrowser;
var s=[(_ef?"":"DEBUG: ")];
for(var i=0;i<_ee.length;++i){
if(!false&&_ee[i]&&_ee[i] instanceof Error){
var msg="["+_ee[i].name+": "+dojo.errorToString(_ee[i])+(_ee[i].fileName?", file: "+_ee[i].fileName:"")+(_ee[i].lineNumber?", line: "+_ee[i].lineNumber:"")+"]";
}else{
try{
var msg=String(_ee[i]);
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
var _f4=[];
for(var _f5 in obj){
try{
_f4.push(_f5+": "+obj[_f5]);
}
catch(E){
_f4.push(_f5+": ERROR - "+E.message);
}
}
_f4.sort();
for(var i=0;i<_f4.length;i++){
dojo.debug(_f4[i]);
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
dojo.ns={namespaces:{},failed:{},loading:{},loaded:{},register:function(_fb,_fc,_fd,_fe){
if(!_fe||!this.namespaces[_fb]){
this.namespaces[_fb]=new dojo.ns.Ns(_fb,_fc,_fd);
}
},allow:function(_ff){
if(this.failed[_ff]){
return false;
}
if((djConfig.excludeNamespace)&&(dojo.lang.inArray(djConfig.excludeNamespace,_ff))){
return false;
}
return ((_ff==this.dojo)||(!djConfig.includeNamespace)||(dojo.lang.inArray(djConfig.includeNamespace,_ff)));
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
dojo.ns.Ns=function(name,_105,_106){
this.name=name;
this.module=_105;
this.resolver=_106;
this._loaded=[];
this._failed=[];
};
dojo.ns.Ns.prototype.resolve=function(name,_108,_109){
if(!this.resolver||djConfig["skipAutoRequire"]){
return false;
}
var _10a=this.resolver(name,_108);
if((_10a)&&(!this._loaded[_10a])&&(!this._failed[_10a])){
var req=dojo.require;
req(_10a,false,true);
if(dojo.hostenv.findModule(_10a,false)){
this._loaded[_10a]=true;
}else{
if(!_109){
dojo.raise("dojo.ns.Ns.resolve: module '"+_10a+"' not found after loading via namespace '"+this.name+"'");
}
this._failed[_10a]=true;
}
}
return Boolean(this._loaded[_10a]);
};
dojo.registerNamespace=function(name,_10d,_10e){
dojo.ns.register.apply(dojo.ns,arguments);
};
dojo.registerNamespaceResolver=function(name,_110){
var n=dojo.ns.namespaces[name];
if(n){
n.resolver=_110;
}
};
dojo.registerNamespaceManifest=function(_112,path,name,_115,_116){
dojo.registerModulePath(name,path);
dojo.registerNamespace(name,_115,_116);
};
dojo.registerNamespace("dojo","dojo.widget");
dojo.provide("dojo.namespaces.dojo");
(function(){
var map={html:{"accordioncontainer":"dojo.widget.AccordionContainer","button":"dojo.widget.Button","chart":"dojo.widget.Chart","checkbox":"dojo.widget.Checkbox","colorpalette":"dojo.widget.ColorPalette","combobox":"dojo.widget.ComboBox","combobutton":"dojo.widget.Button","contentpane":"dojo.widget.ContentPane","contextmenu":"dojo.widget.ContextMenu","currencytextbox":"dojo.widget.CurrencyTextbox","datepicker":"dojo.widget.DatePicker","datetextbox":"dojo.widget.DateTextbox","debugconsole":"dojo.widget.DebugConsole","dialog":"dojo.widget.Dialog","docpane":"dojo.widget.DocPane","dropdownbutton":"dojo.widget.Button","dropdowndatepicker":"dojo.widget.DropdownDatePicker","dropdowntimepicker":"dojo.widget.DropdownTimePicker","emaillisttextbox":"dojo.widget.InternetTextbox","emailtextbox":"dojo.widget.InternetTextbox","editor2":"dojo.widget.Editor2","editor2toolbar":"dojo.widget.Editor2Toolbar","editor":"dojo.widget.Editor","editortree":"dojo.widget.EditorTree","editortreecontextmenu":"dojo.widget.EditorTreeContextMenu","editortreenode":"dojo.widget.EditorTreeNode","filteringtable":"dojo.widget.FilteringTable","fisheyelist":"dojo.widget.FisheyeList","editortreecontroller":"dojo.widget.EditorTreeController","googlemap":"dojo.widget.GoogleMap","editortreeselector":"dojo.widget.EditorTreeSelector","fisheyelist":"dojo.widget.FisheyeList","fisheyelistitem":"dojo.widget.FisheyeList","floatingpane":"dojo.widget.FloatingPane","form":"dojo.widget.Form","hslcolorpicker":"dojo.widget.HslColorPicker","inlineeditbox":"dojo.widget.InlineEditBox","integerspinner":"dojo.widget.IntegerSpinner","integertextbox":"dojo.widget.IntegerTextbox","ipaddresstextbox":"dojo.widget.InternetTextbox","layoutcontainer":"dojo.widget.LayoutContainer","linkpane":"dojo.widget.LinkPane","pagecontainer":"dojo.widget.PageContainer","pagecontroller":"dojo.widget.PageContainer","popupcontainer":"dojo.widget.Menu2","popupmenu2":"dojo.widget.Menu2","menuitem2":"dojo.widget.Menu2","menuseparator2":"dojo.widget.Menu2","menubar2":"dojo.widget.Menu2","menubaritem2":"dojo.widget.Menu2","monthlyCalendar":"dojo.widget.MonthlyCalendar","radiogroup":"dojo.widget.RadioGroup","realnumbertextbox":"dojo.widget.RealNumberTextbox","regexptextbox":"dojo.widget.RegexpTextbox","repeater":"dojo.widget.Repeater","richtext":"dojo.widget.RichText","remotetabcontroller":"dojo.widget.RemoteTabController","resizehandle":"dojo.widget.ResizeHandle","resizabletextarea":"dojo.widget.ResizableTextarea","select":"dojo.widget.Select","slidervertical":"dojo.widget.Slider","sliderhorizontal":"dojo.widget.Slider","slider":"dojo.widget.Slider","slideshow":"dojo.widget.SlideShow","sortabletable":"dojo.widget.SortableTable","splitcontainer":"dojo.widget.SplitContainer","svgbutton":"dojo.widget.SvgButton","tabcontainer":"dojo.widget.TabContainer","tabcontroller":"dojo.widget.TabContainer","taskbar":"dojo.widget.TaskBar","textbox":"dojo.widget.Textbox","timepicker":"dojo.widget.TimePicker","timetextbox":"dojo.widget.DateTextbox","titlepane":"dojo.widget.TitlePane","toaster":"dojo.widget.Toaster","toggler":"dojo.widget.Toggler","toolbar":"dojo.widget.Toolbar","tooltip":"dojo.widget.Tooltip","tree":"dojo.widget.Tree","treebasiccontroller":"dojo.widget.TreeBasicController","treecontextmenu":"dojo.widget.TreeContextMenu","treeselector":"dojo.widget.TreeSelector","treecontrollerextension":"dojo.widget.TreeControllerExtension","treenode":"dojo.widget.TreeNode","treerpccontroller":"dojo.widget.TreeRPCController","treebasiccontrollerv3":"dojo.widget.TreeBasicControllerV3","treecontextmenuv3":"dojo.widget.TreeContextMenuV3","treedeselectondblselect":"dojo.widget.TreeDeselectOnDblselect","treedisablewrapextension":"dojo.widget.TreeDisableWrapExtension","treedndcontrollerv3":"dojo.widget.TreeDndControllerV3","treedociconextension":"dojo.widget.TreeDocIconExtension","treeeditor":"dojo.widget.TreeEditor","treeemphasizeonselect":"dojo.widget.TreeEmphasizeOnSelect","treelinkextension":"dojo.widget.TreeLinkExtension","treeloadingcontrollerv3":"dojo.widget.TreeLoadingControllerV3","treemenuitemv3":"dojo.widget.TreeContextMenuV3","treerpccontrollerv3":"dojo.widget.TreeRpcControllerV3","treeselectorv3":"dojo.widget.TreeSelectorV3","treev3":"dojo.widget.TreeV3","urltextbox":"dojo.widget.InternetTextbox","usphonenumbertextbox":"dojo.widget.UsTextbox","ussocialsecuritynumbertextbox":"dojo.widget.UsTextbox","usstatetextbox":"dojo.widget.UsTextbox","usziptextbox":"dojo.widget.UsTextbox","validationtextbox":"dojo.widget.ValidationTextbox","treeloadingcontroller":"dojo.widget.TreeLoadingController","widget":"dojo.widget.Widget","wizard":"dojo.widget.Wizard","yahoomap":"dojo.widget.YahooMap"},svg:{"chart":"dojo.widget.svg.Chart","hslcolorpicker":"dojo.widget.svg.HslColorPicker"},vml:{"chart":"dojo.widget.vml.Chart"}};
dojo.addDojoNamespaceMapping=function(_118,_119){
map[_118]=_119;
};
function dojoNamespaceResolver(name,_11b){
if(!_11b){
_11b="html";
}
if(!map[_11b]){
return null;
}
return map[_11b][name];
}
dojo.registerNamespaceResolver("dojo",dojoNamespaceResolver);
})();
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_11c,_11d){
if(typeof _11d!="function"){
dojo.raise("dojo.inherits: superclass argument ["+_11d+"] must be a function (subclass: ["+_11c+"']");
}
_11c.prototype=new _11d();
_11c.prototype.constructor=_11c;
_11c.superclass=_11d.prototype;
_11c["super"]=_11d.prototype;
};
dojo.lang._mixin=function(obj,_11f){
var tobj={};
for(var x in _11f){
if((typeof tobj[x]=="undefined")||(tobj[x]!=_11f[x])){
obj[x]=_11f[x];
}
}
if(dojo.render.html.ie&&(typeof (_11f["toString"])=="function")&&(_11f["toString"]!=obj["toString"])&&(_11f["toString"]!=tobj["toString"])){
obj.toString=_11f.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_123){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_126,_127){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_126.prototype,arguments[i]);
}
return _126;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_12a,_12b,_12c,_12d){
if(!dojo.lang.isArrayLike(_12a)&&dojo.lang.isArrayLike(_12b)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_12a;
_12a=_12b;
_12b=temp;
}
var _12f=dojo.lang.isString(_12a);
if(_12f){
_12a=_12a.split("");
}
if(_12d){
var step=-1;
var i=_12a.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_12a.length;
}
if(_12c){
while(i!=end){
if(_12a[i]===_12b){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_12a[i]==_12b){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_133,_134,_135){
return dojo.lang.find(_133,_134,_135,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_136,_137){
return dojo.lang.find(_136,_137)>-1;
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
if(!it){
return false;
}
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction()&&/\{\s*\[native code\]\s*\}/.test(String(it));
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
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_142){
var _143=window,_144=2;
if(!dojo.lang.isFunction(func)){
_143=func;
func=_142;
_142=arguments[2];
_144++;
}
if(dojo.lang.isString(func)){
func=_143[func];
}
var args=[];
for(var i=_144;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_143,args);
},_142);
};
dojo.lang.clearTimeout=function(_147){
dojo.global().clearTimeout(_147);
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
dojo.lang.getObjPathValue=function(_150,_151,_152){
with(dojo.parseObjPath(_150,_151,_152)){
return dojo.evalProp(prop,obj,_152);
}
};
dojo.lang.setObjPathValue=function(_153,_154,_155,_156){
if(arguments.length<4){
_156=true;
}
with(dojo.parseObjPath(_153,_155,_156)){
if(obj&&(_156||(prop in obj))){
obj[prop]=_154;
}
}
};
dojo.provide("dojo.lang.declare");
dojo.lang.declare=function(_157,_158,init,_15a){
if((dojo.lang.isFunction(_15a))||((!_15a)&&(!dojo.lang.isFunction(init)))){
var temp=_15a;
_15a=init;
init=temp;
}
var _15c=[];
if(dojo.lang.isArray(_158)){
_15c=_158;
_158=_15c.shift();
}
if(!init){
init=dojo.evalObjPath(_157,false);
if((init)&&(!dojo.lang.isFunction(init))){
init=null;
}
}
var ctor=dojo.lang.declare._makeConstructor();
var scp=(_158?_158.prototype:null);
if(scp){
scp.prototyping=true;
ctor.prototype=new _158();
scp.prototyping=false;
}
ctor.superclass=scp;
ctor.mixins=_15c;
for(var i=0,l=_15c.length;i<l;i++){
dojo.lang.extend(ctor,_15c[i].prototype);
}
ctor.prototype.initializer=null;
ctor.prototype.declaredClass=_157;
if(dojo.lang.isArray(_15a)){
dojo.lang.extend.apply(dojo.lang,[ctor].concat(_15a));
}else{
dojo.lang.extend(ctor,(_15a)||{});
}
dojo.lang.extend(ctor,dojo.lang.declare._common);
ctor.prototype.constructor=ctor;
ctor.prototype.initializer=(ctor.prototype.initializer)||(init)||(function(){
});
dojo.lang.setObjPathValue(_157,ctor,null,true);
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
},_contextMethod:function(_166,_167,args){
var _169,_16a=this.___proto;
this.___proto=_166;
try{
_169=_166[_167].apply(this,(args||[]));
}
catch(e){
throw e;
}
finally{
this.___proto=_16a;
}
return _169;
},_inherited:function(prop,args){
var p=this._getPropContext();
do{
if((!p.constructor)||(!p.constructor.superclass)){
return;
}
p=p.constructor.superclass;
}while(!(prop in p));
return (dojo.lang.isFunction(p[prop])?this._contextMethod(p,prop,args):p[prop]);
}};
dojo.declare=dojo.lang.declare;
dojo.provide("dojo.dnd.DragAndDrop");
dojo.declare("dojo.dnd.DragSource",null,{type:"",onDragEnd:function(){
},onDragStart:function(){
},onSelected:function(){
},unregister:function(){
dojo.dnd.dragManager.unregisterDragSource(this);
},reregister:function(){
dojo.dnd.dragManager.registerDragSource(this);
}},function(){
var dm=dojo.dnd.dragManager;
if(dm["registerDragSource"]){
dm.registerDragSource(this);
}
});
dojo.declare("dojo.dnd.DragObject",null,{type:"",onDragStart:function(){
},onDragMove:function(){
},onDragOver:function(){
},onDragOut:function(){
},onDragEnd:function(){
},onDragLeave:this.onDragOut,onDragEnter:this.onDragOver,ondragout:this.onDragOut,ondragover:this.onDragOver},function(){
var dm=dojo.dnd.dragManager;
if(dm["registerDragObject"]){
dm.registerDragObject(this);
}
});
dojo.declare("dojo.dnd.DropTarget",null,{acceptsType:function(type){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
if(!dojo.lang.inArray(this.acceptedTypes,type)){
return false;
}
}
return true;
},accepts:function(_171){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
for(var i=0;i<_171.length;i++){
if(!dojo.lang.inArray(this.acceptedTypes,_171[i].type)){
return false;
}
}
}
return true;
},unregister:function(){
dojo.dnd.dragManager.unregisterDropTarget(this);
},onDragOver:function(){
},onDragOut:function(){
},onDragMove:function(){
},onDropStart:function(){
},onDrop:function(){
},onDropEnd:function(){
}},function(){
if(this.constructor==dojo.dnd.DropTarget){
return;
}
this.acceptedTypes=[];
dojo.dnd.dragManager.registerDropTarget(this);
});
dojo.dnd.DragEvent=function(){
this.dragSource=null;
this.dragObject=null;
this.target=null;
this.eventStatus="success";
};
dojo.declare("dojo.dnd.DragManager",null,{selectedSources:[],dragObjects:[],dragSources:[],registerDragSource:function(){
},dropTargets:[],registerDropTarget:function(){
},lastDragTarget:null,currentDragTarget:null,onKeyDown:function(){
},onMouseOut:function(){
},onMouseMove:function(){
},onMouseUp:function(){
}});
dojo.provide("dojo.lang.array");
dojo.lang.has=function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
};
dojo.lang.isEmpty=function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _177=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_177++;
break;
}
}
return _177==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
};
dojo.lang.map=function(arr,obj,_17b){
var _17c=dojo.lang.isString(arr);
if(_17c){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_17b)){
_17b=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_17b){
var _17d=obj;
obj=_17b;
_17b=_17d;
}
}
if(Array.map){
var _17e=Array.map(arr,_17b,obj);
}else{
var _17e=[];
for(var i=0;i<arr.length;++i){
_17e.push(_17b.call(obj,arr[i]));
}
}
if(_17c){
return _17e.join("");
}else{
return _17e;
}
};
dojo.lang.reduce=function(arr,_181,obj,_183){
var _184=_181;
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_184=_183.call(ob,_184,val);
});
return _184;
};
dojo.lang.forEach=function(_187,_188,_189){
if(dojo.lang.isString(_187)){
_187=_187.split("");
}
if(Array.forEach){
Array.forEach(_187,_188,_189);
}else{
if(!_189){
_189=dj_global;
}
for(var i=0,l=_187.length;i<l;i++){
_188.call(_189,_187[i],i,_187);
}
}
};
dojo.lang._everyOrSome=function(_18c,arr,_18e,_18f){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_18c?"every":"some"](arr,_18e,_18f);
}else{
if(!_18f){
_18f=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _192=_18e.call(_18f,arr[i],i,arr);
if(_18c&&!_192){
return false;
}else{
if((!_18c)&&(_192)){
return true;
}
}
}
return Boolean(_18c);
}
};
dojo.lang.every=function(arr,_194,_195){
return this._everyOrSome(true,arr,_194,_195);
};
dojo.lang.some=function(arr,_197,_198){
return this._everyOrSome(false,arr,_197,_198);
};
dojo.lang.filter=function(arr,_19a,_19b){
var _19c=dojo.lang.isString(arr);
if(_19c){
arr=arr.split("");
}
var _19d;
if(Array.filter){
_19d=Array.filter(arr,_19a,_19b);
}else{
if(!_19b){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_19b=dj_global;
}
_19d=[];
for(var i=0;i<arr.length;i++){
if(_19a.call(_19b,arr[i],i,arr)){
_19d.push(arr[i]);
}
}
}
if(_19c){
return _19d.join("");
}else{
return _19d;
}
};
dojo.lang.unnest=function(){
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
};
dojo.lang.toArray=function(_1a2,_1a3){
var _1a4=[];
for(var i=_1a3||0;i<_1a2.length;i++){
_1a4.push(_1a2[i]);
}
return _1a4;
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_1a6,_1a7){
var fcn=(dojo.lang.isString(_1a7)?_1a6[_1a7]:_1a7)||function(){
};
return function(){
return fcn.apply(_1a6,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_1a9,_1aa,_1ab){
var nso=(_1aa||dojo.lang.anon);
if((_1ab)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_1a9){
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
nso[ret]=_1a9;
return ret;
};
dojo.lang.forward=function(_1af){
return function(){
return this[_1af].apply(this,arguments);
};
};
dojo.lang.curry=function(ns,func){
var _1b2=[];
ns=ns||dj_global;
if(dojo.lang.isString(func)){
func=ns[func];
}
for(var x=2;x<arguments.length;x++){
_1b2.push(arguments[x]);
}
var _1b4=(func["__preJoinArity"]||func.length)-_1b2.length;
function gather(_1b5,_1b6,_1b7){
var _1b8=_1b7;
var _1b9=_1b6.slice(0);
for(var x=0;x<_1b5.length;x++){
_1b9.push(_1b5[x]);
}
_1b7=_1b7-_1b5.length;
if(_1b7<=0){
var res=func.apply(ns,_1b9);
_1b7=_1b8;
return res;
}else{
return function(){
return gather(arguments,_1b9,_1b7);
};
}
}
return gather([],_1b2,_1b4);
};
dojo.lang.curryArguments=function(ns,func,args,_1bf){
var _1c0=[];
var x=_1bf||0;
for(x=_1bf;x<args.length;x++){
_1c0.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[ns,func].concat(_1c0));
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
dojo.lang.delayThese=function(farr,cb,_1c6,_1c7){
if(!farr.length){
if(typeof _1c7=="function"){
_1c7();
}
return;
}
if((typeof _1c6=="undefined")&&(typeof cb=="number")){
_1c6=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_1c6){
_1c6=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_1c6,_1c7);
},_1c6);
};
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_1c9){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false};
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
var _1cc=dl.nameAnonFunc(args[2],ao.adviceObj,_1c9);
ao.adviceFunc=_1cc;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _1cc=dl.nameAnonFunc(args[0],ao.srcObj,_1c9);
ao.srcFunc=_1cc;
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
var _1cc=dl.nameAnonFunc(args[1],dj_global,_1c9);
ao.srcFunc=_1cc;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _1cc=dl.nameAnonFunc(args[3],dj_global,_1c9);
ao.adviceObj=dj_global;
ao.adviceFunc=_1cc;
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
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _1cc=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_1c9);
ao.aroundFunc=_1cc;
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
var _1ce={};
for(var x in ao){
_1ce[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_1ce.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_1ce));
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
var _1d6;
if((arguments.length==1)&&(typeof a1=="object")){
_1d6=a1;
}else{
_1d6={srcObj:a1,srcFunc:a2};
}
_1d6.adviceFunc=function(){
var _1d7=[];
for(var x=0;x<arguments.length;x++){
_1d7.push(arguments[x]);
}
dojo.debug("("+_1d6.srcObj+")."+_1d6.srcFunc,":",_1d7.join(", "));
};
this.kwConnect(_1d6);
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
this._kwConnectImpl=function(_1de,_1df){
var fn=(_1df)?"disconnect":"connect";
if(typeof _1de["srcFunc"]=="function"){
_1de.srcObj=_1de["srcObj"]||dj_global;
var _1e1=dojo.lang.nameAnonFunc(_1de.srcFunc,_1de.srcObj,true);
_1de.srcFunc=_1e1;
}
if(typeof _1de["adviceFunc"]=="function"){
_1de.adviceObj=_1de["adviceObj"]||dj_global;
var _1e1=dojo.lang.nameAnonFunc(_1de.adviceFunc,_1de.adviceObj,true);
_1de.adviceFunc=_1e1;
}
_1de.srcObj=_1de["srcObj"]||dj_global;
_1de.adviceObj=_1de["adviceObj"]||_1de["targetObj"]||dj_global;
_1de.adviceFunc=_1de["adviceFunc"]||_1de["targetFunc"];
return dojo.event[fn](_1de);
};
this.kwConnect=function(_1e2){
return this._kwConnectImpl(_1e2,false);
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
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
return mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
};
this.kwDisconnect=function(_1e5){
return this._kwConnectImpl(_1e5,true);
};
};
dojo.event.MethodInvocation=function(_1e6,obj,args){
this.jp_=_1e6;
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
dojo.event.MethodJoinPoint=function(obj,_1ee){
this.object=obj||dj_global;
this.methodname=_1ee;
this.methodfunc=this.object[_1ee];
this.squelch=false;
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_1f0){
if(!obj){
obj=dj_global;
}
if(!obj[_1f0]){
obj[_1f0]=function(){
};
if(!obj[_1f0]){
dojo.raise("Cannot set do-nothing method on that object "+_1f0);
}
}else{
if((!dojo.lang.isFunction(obj[_1f0]))&&(!dojo.lang.isAlien(obj[_1f0]))){
return null;
}
}
var _1f1=_1f0+"$joinpoint";
var _1f2=_1f0+"$joinpoint$method";
var _1f3=obj[_1f1];
if(!_1f3){
var _1f4=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_1f4=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_1f1,_1f2,_1f0]);
}
}
var _1f5=obj[_1f0].length;
obj[_1f2]=obj[_1f0];
_1f3=obj[_1f1]=new dojo.event.MethodJoinPoint(obj,_1f2);
obj[_1f0]=function(){
var args=[];
if((_1f4)&&(!arguments.length)){
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
if((x==0)&&(_1f4)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _1f3.run.apply(_1f3,args);
};
obj[_1f0].__preJoinArity=_1f5;
}
return _1f3;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _1fb=[];
for(var x=0;x<args.length;x++){
_1fb[x]=args[x];
}
var _1fd=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _1ff=marr[0]||dj_global;
var _200=marr[1];
if(!_1ff[_200]){
dojo.raise("function \""+_200+"\" does not exist on \""+_1ff+"\"");
}
var _201=marr[2]||dj_global;
var _202=marr[3];
var msg=marr[6];
var _204;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _1ff[_200].apply(_1ff,to.args);
}};
to.args=_1fb;
var _206=parseInt(marr[4]);
var _207=((!isNaN(_206))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _20a=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_1fd(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_202){
_201[_202].call(_201,to);
}else{
if((_207)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_1ff[_200].call(_1ff,to);
}else{
_1ff[_200].apply(_1ff,args);
}
},_206);
}else{
if(msg){
_1ff[_200].call(_1ff,to);
}else{
_1ff[_200].apply(_1ff,args);
}
}
}
};
var _20d=function(){
if(this.squelch){
try{
return _1fd.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _1fd.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_20d);
}
var _20e;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_20e=mi.proceed();
}else{
if(this.methodfunc){
_20e=this.object[this.methodname].apply(this.object,args);
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
dojo.lang.forEach(this.after.concat(new Array()),_20d);
}
return (this.methodfunc)?_20e:null;
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
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"]);
},addAdvice:function(_213,_214,_215,_216,_217,_218,once,_21a,rate,_21c){
var arr=this.getArr(_217);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_213,_214,_215,_216,_21a,rate,_21c];
if(once){
if(this.hasAdvice(_213,_214,_217,arr)>=0){
return;
}
}
if(_218=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_21f,_220,_221,arr){
if(!arr){
arr=this.getArr(_221);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _220=="object")?(new String(_220)).toString():_220;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_21f)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_227,_228,_229,once){
var arr=this.getArr(_229);
var ind=this.hasAdvice(_227,_228,_229,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_227,_228,_229,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_22d){
if(!this.topics[_22d]){
this.topics[_22d]=new this.TopicImpl(_22d);
}
return this.topics[_22d];
};
this.registerPublisher=function(_22e,obj,_230){
var _22e=this.getTopic(_22e);
_22e.registerPublisher(obj,_230);
};
this.subscribe=function(_231,obj,_233){
var _231=this.getTopic(_231);
_231.subscribe(obj,_233);
};
this.unsubscribe=function(_234,obj,_236){
var _234=this.getTopic(_234);
_234.unsubscribe(obj,_236);
};
this.destroy=function(_237){
this.getTopic(_237).destroy();
delete this.topics[_237];
};
this.publishApply=function(_238,args){
var _238=this.getTopic(_238);
_238.sendMessage.apply(_238,args);
};
this.publish=function(_23a,_23b){
var _23a=this.getTopic(_23a);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_23a.sendMessage.apply(_23a,args);
};
};
dojo.event.topic.TopicImpl=function(_23e){
this.topicName=_23e;
this.subscribe=function(_23f,_240){
var tf=_240||_23f;
var to=(!_240)?dj_global:_23f;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_243,_244){
var tf=(!_244)?_243:_244;
var to=(!_244)?null:_243;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_247){
this._getJoinPoint().squelch=_247;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_248,_249){
dojo.event.connect(_248,_249,this,"sendMessage");
};
this.sendMessage=function(_24a){
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
this.clobber=function(_24d){
var na;
var tna;
if(_24d){
tna=_24d.all||_24d.getElementsByTagName("*");
na=[_24d];
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
var _251={};
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
var _255=0;
this.normalizedEventName=function(_256){
switch(_256){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _256;
break;
default:
return _256.toLowerCase();
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
this.addClobberNodeAttrs=function(node,_25a){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_25a.length;x++){
node.__clobberAttrs__.push(_25a[x]);
}
};
this.removeListener=function(node,_25d,fp,_25f){
if(!_25f){
var _25f=false;
}
_25d=dojo.event.browser.normalizedEventName(_25d);
if((_25d=="onkey")||(_25d=="key")){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_25f);
}
_25d="onkeypress";
}
if(_25d.substr(0,2)=="on"){
_25d=_25d.substr(2);
}
if(node.removeEventListener){
node.removeEventListener(_25d,fp,_25f);
}
};
this.addListener=function(node,_261,fp,_263,_264){
if(!node){
return;
}
if(!_263){
var _263=false;
}
_261=dojo.event.browser.normalizedEventName(_261);
if((_261=="onkey")||(_261=="key")){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_263,_264);
}
_261="onkeypress";
}
if(_261.substr(0,2)!="on"){
_261="on"+_261;
}
if(!_264){
var _265=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_263){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_265=fp;
}
if(node.addEventListener){
node.addEventListener(_261.substr(2),_265,_263);
return _265;
}else{
if(typeof node[_261]=="function"){
var _268=node[_261];
node[_261]=function(e){
_268(e);
return _265(e);
};
}else{
node[_261]=_265;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_261]);
}
return _265;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_26b,_26c){
if(typeof _26b!="function"){
dojo.raise("listener not a function: "+_26b);
}
dojo.event.browser.currentEvent.currentTarget=_26c;
return _26b.call(_26c,dojo.event.browser.currentEvent);
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
this.fixEvent=function(evt,_26f){
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
var _271=evt.keyCode;
if(_271>=65&&_271<=90&&evt.shiftKey==false){
_271+=32;
}
if(_271>=1&&_271<=26&&evt.ctrlKey){
_271+=96;
}
evt.key=String.fromCharCode(_271);
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
var _271=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_271+=32;
}
evt.key=String.fromCharCode(_271);
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
default:
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
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
evt.currentTarget=(_26f?_26f:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _273=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_273.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_273.scrollTop||0);
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
catch(E){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _276=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_276.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_278,_279){
var node=_278.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_279&&node&&node.tagName&&node.tagName.toLowerCase()!=_279.toLowerCase()){
node=dojo.dom.nextElement(node,_279);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_27b,_27c){
var node=_27b.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_27c&&node&&node.tagName&&node.tagName.toLowerCase()!=_27c.toLowerCase()){
node=dojo.dom.prevElement(node,_27c);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_27f){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_27f&&_27f.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_27f);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_281){
if(!node){
return null;
}
if(_281){
_281=_281.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_281&&_281.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_281);
}
return node;
};
dojo.dom.moveChildren=function(_282,_283,trim){
var _285=0;
if(trim){
while(_282.hasChildNodes()&&_282.firstChild.nodeType==dojo.dom.TEXT_NODE){
_282.removeChild(_282.firstChild);
}
while(_282.hasChildNodes()&&_282.lastChild.nodeType==dojo.dom.TEXT_NODE){
_282.removeChild(_282.lastChild);
}
}
while(_282.hasChildNodes()){
_283.appendChild(_282.firstChild);
_285++;
}
return _285;
};
dojo.dom.copyChildren=function(_286,_287,trim){
var _289=_286.cloneNode(true);
return this.moveChildren(_289,_287,trim);
};
dojo.dom.removeChildren=function(node){
var _28b=node.childNodes.length;
while(node.hasChildNodes()){
node.removeChild(node.firstChild);
}
return _28b;
};
dojo.dom.replaceChildren=function(node,_28d){
dojo.dom.removeChildren(node);
node.appendChild(_28d);
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_290,_291){
var _292=[];
var _293=(_290&&(_290 instanceof Function||typeof _290=="function"));
while(node){
if(!_293||_290(node)){
_292.push(node);
}
if(_291&&_292.length>0){
return _292[0];
}
node=node.parentNode;
}
if(_291){
return null;
}
return _292;
};
dojo.dom.getAncestorsByTag=function(node,tag,_296){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_296);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_29b,_29c){
if(_29c&&node){
node=node.parentNode;
}
while(node){
if(node==_29b){
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
var _29f=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _2a0=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_2a0.length;i++){
try{
doc=new ActiveXObject(_2a0[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_29f.implementation)&&(_29f.implementation.createDocument)){
doc=_29f.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_2a3){
if(!_2a3){
_2a3="text/xml";
}
if(!dj_undef("DOMParser")){
var _2a4=new DOMParser();
return _2a4.parseFromString(str,_2a3);
}else{
if(!dj_undef("ActiveXObject")){
var _2a5=dojo.dom.createDocument();
if(_2a5){
_2a5.async=false;
_2a5.loadXML(str);
return _2a5;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _2a6=dojo.doc();
if(_2a6.createElement){
var tmp=_2a6.createElement("xml");
tmp.innerHTML=str;
if(_2a6.implementation&&_2a6.implementation.createDocument){
var _2a8=_2a6.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_2a8.importNode(tmp.childNodes.item(i),true);
}
return _2a8;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_2ab){
if(_2ab.firstChild){
_2ab.insertBefore(node,_2ab.firstChild);
}else{
_2ab.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_2ae){
if(_2ae!=true&&(node===ref||node.nextSibling===ref)){
return false;
}
var _2af=ref.parentNode;
_2af.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_2b2){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_2b2!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_2b2);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_2b6){
if((!node)||(!ref)||(!_2b6)){
return false;
}
switch(_2b6.toLowerCase()){
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
dojo.dom.insertAtIndex=function(node,_2b8,_2b9){
var _2ba=_2b8.childNodes;
if(!_2ba.length){
_2b8.appendChild(node);
return true;
}
var _2bb=null;
for(var i=0;i<_2ba.length;i++){
var _2bd=_2ba.item(i)["getAttribute"]?parseInt(_2ba.item(i).getAttribute("dojoinsertionindex")):-1;
if(_2bd<_2b9){
_2bb=_2ba.item(i);
}
}
if(_2bb){
return dojo.dom.insertAfter(node,_2bb);
}else{
return dojo.dom.insertBefore(node,_2ba.item(0));
}
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _2c0=dojo.doc();
dojo.dom.replaceChildren(node,_2c0.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _2c1="";
if(node==null){
return _2c1;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_2c1+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_2c1+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _2c1;
}
};
dojo.dom.hasParent=function(node){
return node&&node.parentNode&&dojo.dom.isNode(node.parentNode);
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
dojo.dom.setAttributeNS=function(elem,_2c7,_2c8,_2c9){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_2c7,_2c8,_2c9);
}else{
var _2ca=elem.ownerDocument;
var _2cb=_2ca.createNode(2,_2c8,_2c7);
_2cb.nodeValue=_2c9;
elem.setAttributeNode(_2cb);
}
};
dojo.provide("dojo.html.common");
dojo.lang.mixin(dojo.html,dojo.dom);
dojo.html.body=function(){
dojo.deprecated("dojo.html.body() moved to dojo.body()","0.5");
return dojo.body();
};
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
var _2ce=dojo.global();
var _2cf=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_2cf.documentElement.clientWidth;
h=_2ce.innerHeight;
}else{
if(!dojo.render.html.opera&&_2ce.innerWidth){
w=_2ce.innerWidth;
h=_2ce.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_2cf,"documentElement.clientWidth")){
var w2=_2cf.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_2cf.documentElement.clientHeight;
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
var _2d3=dojo.global();
var _2d4=dojo.doc();
var top=_2d3.pageYOffset||_2d4.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_2d3.pageXOffset||_2d4.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _2d9=dojo.doc();
var _2da=dojo.byId(node);
type=type.toLowerCase();
while((_2da)&&(_2da.nodeName.toLowerCase()!=type)){
if(_2da==(_2d9["body"]||_2d9["documentElement"])){
return null;
}
_2da=_2da.parentNode;
}
return _2da;
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
var _2e2={x:0,y:0};
if(e.pageX||e.pageY){
_2e2.x=e.pageX;
_2e2.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_2e2.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_2e2.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _2e2;
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
var _2e7=dojo.doc().createElement("script");
_2e7.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_2e7);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.html._callDeprecated=function(_2ea,_2eb,args,_2ed,_2ee){
dojo.deprecated("dojo.html."+_2ea,"replaced by dojo.html."+_2eb+"("+(_2ed?"node, {"+_2ed+": "+_2ed+"}":"")+")"+(_2ee?"."+_2ee:""),"0.5");
var _2ef=[];
if(_2ed){
var _2f0={};
_2f0[_2ed]=args[1];
_2ef.push(args[0]);
_2ef.push(_2f0);
}else{
_2ef=args;
}
var ret=dojo.html[_2eb].apply(dojo.html,args);
if(_2ee){
return ret[_2ee];
}else{
return ret;
}
};
dojo.html.getViewportWidth=function(){
return dojo.html._callDeprecated("getViewportWidth","getViewport",arguments,null,"width");
};
dojo.html.getViewportHeight=function(){
return dojo.html._callDeprecated("getViewportHeight","getViewport",arguments,null,"height");
};
dojo.html.getViewportSize=function(){
return dojo.html._callDeprecated("getViewportSize","getViewport",arguments);
};
dojo.html.getScrollTop=function(){
return dojo.html._callDeprecated("getScrollTop","getScroll",arguments,null,"top");
};
dojo.html.getScrollLeft=function(){
return dojo.html._callDeprecated("getScrollLeft","getScroll",arguments,null,"left");
};
dojo.html.getScrollOffset=function(){
return dojo.html._callDeprecated("getScrollOffset","getScroll",arguments,null,"offset");
};
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_2f3,uri){
var loc=dojo.hostenv.getModuleSymbols(_2f3).join("/");
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
var _2f8=new dojo.uri.Uri(arguments[i].toString());
var _2f9=new dojo.uri.Uri(uri.toString());
if((_2f8.path=="")&&(_2f8.scheme==null)&&(_2f8.authority==null)&&(_2f8.query==null)){
if(_2f8.fragment!=null){
_2f9.fragment=_2f8.fragment;
}
_2f8=_2f9;
}else{
if(_2f8.scheme==null){
_2f8.scheme=_2f9.scheme;
if(_2f8.authority==null){
_2f8.authority=_2f9.authority;
if(_2f8.path.charAt(0)!="/"){
var path=_2f9.path.substring(0,_2f9.path.lastIndexOf("/")+1)+_2f8.path;
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
_2f8.path=segs.join("/");
}
}
}
}
uri="";
if(_2f8.scheme!=null){
uri+=_2f8.scheme+":";
}
if(_2f8.authority!=null){
uri+="//"+_2f8.authority;
}
uri+=_2f8.path;
if(_2f8.query!=null){
uri+="?"+_2f8.query;
}
if(_2f8.fragment!=null){
uri+="#"+_2f8.fragment;
}
}
this.uri=uri.toString();
var _2fd="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_2fd));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_2fd="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_2fd));
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
dojo.html.hasClass=function(node,_304){
return (new RegExp("(^|\\s+)"+_304+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_306){
_306+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_306);
};
dojo.html.addClass=function(node,_308){
if(dojo.html.hasClass(node,_308)){
return false;
}
_308=(dojo.html.getClass(node)+" "+_308).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_308);
};
dojo.html.setClass=function(node,_30a){
node=dojo.byId(node);
var cs=new String(_30a);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_30a);
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
dojo.html.removeClass=function(node,_30d,_30e){
try{
if(!_30e){
var _30f=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_30d+"(\\s+|$)"),"$1$2");
}else{
var _30f=dojo.html.getClass(node).replace(_30d,"");
}
dojo.html.setClass(node,_30f);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_311,_312){
dojo.html.removeClass(node,_312);
dojo.html.addClass(node,_311);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_313,_314,_315,_316,_317){
_317=false;
var _318=dojo.doc();
_314=dojo.byId(_314)||_318;
var _319=_313.split(/\s+/g);
var _31a=[];
if(_316!=1&&_316!=2){
_316=0;
}
var _31b=new RegExp("(\\s|^)(("+_319.join(")|(")+"))(\\s|$)");
var _31c=_319.join(" ").length;
var _31d=[];
if(!_317&&_318.evaluate){
var _31e=".//"+(_315||"*")+"[contains(";
if(_316!=dojo.html.classMatchType.ContainsAny){
_31e+="concat(' ',@class,' '), ' "+_319.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_316==2){
_31e+=" and string-length(@class)="+_31c+"]";
}else{
_31e+="]";
}
}else{
_31e+="concat(' ',@class,' '), ' "+_319.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _31f=_318.evaluate(_31e,_314,null,XPathResult.ANY_TYPE,null);
var _320=_31f.iterateNext();
while(_320){
try{
_31d.push(_320);
_320=_31f.iterateNext();
}
catch(e){
break;
}
}
return _31d;
}else{
if(!_315){
_315="*";
}
_31d=_314.getElementsByTagName(_315);
var node,i=0;
outer:
while(node=_31d[i++]){
var _323=dojo.html.getClasses(node);
if(_323.length==0){
continue outer;
}
var _324=0;
for(var j=0;j<_323.length;j++){
if(_31b.test(_323[j])){
if(_316==dojo.html.classMatchType.ContainsAny){
_31a.push(node);
continue outer;
}else{
_324++;
}
}else{
if(_316==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_324==_319.length){
if((_316==dojo.html.classMatchType.IsOnly)&&(_324==_323.length)){
_31a.push(node);
}else{
if(_316==dojo.html.classMatchType.ContainsAll){
_31a.push(node);
}
}
}
}
return _31a;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_326){
var arr=_326.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_32a){
return _32a.replace(/([A-Z])/g,"-$1").toLowerCase();
};
dojo.html.getComputedStyle=function(node,_32c,_32d){
node=dojo.byId(node);
var _32c=dojo.html.toSelectorCase(_32c);
var _32e=dojo.html.toCamelCase(_32c);
if(!node||!node.style){
return _32d;
}else{
if(document.defaultView&&dojo.html.isDescendantOf(node,node.ownerDocument)){
try{
var cs=document.defaultView.getComputedStyle(node,"");
if(cs){
return cs.getPropertyValue(_32c);
}
}
catch(e){
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_32c);
}else{
return _32d;
}
}
}else{
if(node.currentStyle){
return node.currentStyle[_32e];
}
}
}
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_32c);
}else{
return _32d;
}
};
dojo.html.getStyleProperty=function(node,_331){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_331)]:undefined);
};
dojo.html.getStyle=function(node,_333){
var _334=dojo.html.getStyleProperty(node,_333);
return (_334?_334:dojo.html.getComputedStyle(node,_333));
};
dojo.html.setStyle=function(node,_336,_337){
node=dojo.byId(node);
if(node&&node.style){
var _338=dojo.html.toCamelCase(_336);
node.style[_338]=_337;
}
};
dojo.html.setStyleText=function(_339,text){
try{
_339.style.cssText=text;
}
catch(e){
_339.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_33b,_33c){
if(!_33c.style.cssText){
_33b.setAttribute("style",_33c.getAttribute("style"));
}else{
_33b.style.cssText=_33c.style.cssText;
}
dojo.html.addClass(_33b,dojo.html.getClass(_33c));
};
dojo.html.getUnitValue=function(node,_33e,_33f){
var s=dojo.html.getComputedStyle(node,_33e);
if((!s)||((s=="auto")&&(_33f))){
return {value:0,units:"px"};
}
var _341=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_341){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_341[1]),units:_341[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
dojo.html.getPixelValue=function(node,_343,_344){
var _345=dojo.html.getUnitValue(node,_343,_344);
if(isNaN(_345.value)){
return 0;
}
if((_345.value)&&(_345.units!="px")){
return NaN;
}
return _345.value;
};
dojo.html.setPositivePixelValue=function(node,_347,_348){
if(isNaN(_348)){
return false;
}
node.style[_347]=Math.max(0,_348)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_349,_34a,_34b){
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
_34b=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_34b=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_349+" { "+_34a+" }";
return dojo.html.styleSheet.insertRule(rule,_34b);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_349,_34a,_34b);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_34d){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_34d){
_34d=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_34d);
}
}else{
if(document.styleSheets[0]){
if(!_34d){
_34d=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_34d);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_350,_351){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _352=dojo.hostenv.getText(URI,false,_351);
if(_352===null){
return;
}
_352=dojo.html.fixPathsInCssText(_352,URI);
if(_350){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_352)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _357=doc.getElementsByTagName("style");
for(var i=0;i<_357.length;i++){
if(_357[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _358=dojo.html.insertCssText(_352);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_352,"nodeRef":_358});
if(_358&&djConfig.isDebug){
_358.setAttribute("dbgHref",URI);
}
return _358;
};
dojo.html.insertCssText=function(_359,doc,URI){
if(!_359){
return;
}
if(!doc){
doc=document;
}
if(URI){
_359=dojo.html.fixPathsInCssText(_359,URI);
}
var _35c=doc.createElement("style");
_35c.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_35c);
}
if(_35c.styleSheet){
_35c.styleSheet.cssText=_359;
}else{
var _35e=doc.createTextNode(_359);
_35c.appendChild(_35e);
}
return _35c;
};
dojo.html.fixPathsInCssText=function(_35f,URI){
function iefixPathsInCssText(){
var _361=/AlphaImageLoader\(src\=['"]([\t\s\w()\/.\\'"-:#=&?~]*)['"]/;
while(_362=_361.exec(_35f)){
url=_362[1].replace(_364,"$2");
if(!_365.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_35f.substring(0,_362.index)+"AlphaImageLoader(src='"+url+"'";
_35f=_35f.substr(_362.index+_362[0].length);
}
return str+_35f;
}
if(!_35f||!URI){
return;
}
var _362,str="",url="";
var _367=/url\(\s*([\t\s\w()\/.\\'"-:#=&?]+)\s*\)/;
var _365=/(file|https?|ftps?):\/\//;
var _364=/^[\s]*(['"]?)([\w()\/.\\'"-:#=&?]*)\1[\s]*?$/;
if(dojo.render.html.ie55||dojo.render.html.ie60){
_35f=iefixPathsInCssText();
}
while(_362=_367.exec(_35f)){
url=_362[1].replace(_364,"$2");
if(!_365.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_35f.substring(0,_362.index)+"url("+url+")";
_35f=_35f.substr(_362.index+_362[0].length);
}
return str+_35f;
};
dojo.html.setActiveStyleSheet=function(_368){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_368){
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
var _374={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _374){
if(_374[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.provide("dojo.html.display");
dojo.html._toggle=function(node,_377,_378){
node=dojo.byId(node);
_378(node,!_377(node));
return _377(node);
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
dojo.html.setShowing=function(node,_37d){
dojo.html[(_37d?"show":"hide")](node);
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
dojo.html.setDisplay=function(node,_383){
dojo.html.setStyle(node,"display",((_383 instanceof String||typeof _383=="string")?_383:(_383?dojo.html.suggestDisplayByTagName(node):"none")));
};
dojo.html.isDisplayed=function(node){
return (dojo.html.getComputedStyle(node,"display")!="none");
};
dojo.html.toggleDisplay=function(node){
return dojo.html._toggle(node,dojo.html.isDisplayed,dojo.html.setDisplay);
};
dojo.html.setVisibility=function(node,_387){
dojo.html.setStyle(node,"visibility",((_387 instanceof String||typeof _387=="string")?_387:(_387?"visible":"hidden")));
};
dojo.html.isVisible=function(node){
return (dojo.html.getComputedStyle(node,"visibility")!="hidden");
};
dojo.html.toggleVisibility=function(node){
return dojo.html._toggle(node,dojo.html.isVisible,dojo.html.setVisibility);
};
dojo.html.setOpacity=function(node,_38b,_38c){
node=dojo.byId(node);
var h=dojo.render.html;
if(!_38c){
if(_38b>=1){
if(h.ie){
dojo.html.clearOpacity(node);
return;
}else{
_38b=0.999999;
}
}else{
if(_38b<0){
_38b=0;
}
}
}
if(h.ie){
if(node.nodeName.toLowerCase()=="tr"){
var tds=node.getElementsByTagName("td");
for(var x=0;x<tds.length;x++){
tds[x].style.filter="Alpha(Opacity="+_38b*100+")";
}
}
node.style.filter="Alpha(Opacity="+_38b*100+")";
}else{
if(h.moz){
node.style.opacity=_38b;
node.style.MozOpacity=_38b;
}else{
if(h.safari){
node.style.opacity=_38b;
node.style.KhtmlOpacity=_38b;
}else{
node.style.opacity=_38b;
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
var _398=0;
while(node){
if(dojo.html.getComputedStyle(node,"position")=="fixed"){
return 0;
}
var val=node[prop];
if(val){
_398+=val-0;
if(node==dojo.body()){
break;
}
}
node=node.parentNode;
}
return _398;
};
dojo.html.setStyleAttributes=function(node,_39b){
node=dojo.byId(node);
var _39c=_39b.replace(/(;)?\s*$/,"").split(";");
for(var i=0;i<_39c.length;i++){
var _39e=_39c[i].split(":");
var name=_39e[0].replace(/\s*$/,"").replace(/^\s*/,"").toLowerCase();
var _3a0=_39e[1].replace(/\s*$/,"").replace(/^\s*/,"");
switch(name){
case "opacity":
dojo.html.setOpacity(node,_3a0);
break;
case "content-height":
dojo.html.setContentBox(node,{height:_3a0});
break;
case "content-width":
dojo.html.setContentBox(node,{width:_3a0});
break;
case "outer-height":
dojo.html.setMarginBox(node,{height:_3a0});
break;
case "outer-width":
dojo.html.setMarginBox(node,{width:_3a0});
break;
default:
node.style[dojo.html.toCamelCase(name)]=_3a0;
}
}
};
dojo.html.boxSizing={MARGIN_BOX:"margin-box",BORDER_BOX:"border-box",PADDING_BOX:"padding-box",CONTENT_BOX:"content-box"};
dojo.html.getAbsolutePosition=dojo.html.abs=function(node,_3a2,_3a3){
node=dojo.byId(node,node.ownerDocument);
var ret={x:0,y:0};
var bs=dojo.html.boxSizing;
if(!_3a3){
_3a3=bs.CONTENT_BOX;
}
var _3a6=2;
var _3a7;
switch(_3a3){
case bs.MARGIN_BOX:
_3a7=3;
break;
case bs.BORDER_BOX:
_3a7=2;
break;
case bs.PADDING_BOX:
default:
_3a7=1;
break;
case bs.CONTENT_BOX:
_3a7=0;
break;
}
var h=dojo.render.html;
var db=document["body"]||document["documentElement"];
if(h.ie){
with(node.getBoundingClientRect()){
ret.x=left-2;
ret.y=top-2;
}
}else{
if(document.getBoxObjectFor){
_3a6=1;
try{
var bo=document.getBoxObjectFor(node);
ret.x=bo.x-dojo.html.sumAncestorProperties(node,"scrollLeft");
ret.y=bo.y-dojo.html.sumAncestorProperties(node,"scrollTop");
}
catch(e){
}
}else{
if(node["offsetParent"]){
var _3ab;
if((h.safari)&&(node.style.getPropertyValue("position")=="absolute")&&(node.parentNode==db)){
_3ab=db;
}else{
_3ab=db.parentNode;
}
if(node.parentNode!=db){
var nd=node;
if(dojo.render.html.opera){
nd=db;
}
ret.x-=dojo.html.sumAncestorProperties(nd,"scrollLeft");
ret.y-=dojo.html.sumAncestorProperties(nd,"scrollTop");
}
var _3ad=node;
do{
var n=_3ad["offsetLeft"];
if(!h.opera||n>0){
ret.x+=isNaN(n)?0:n;
}
var m=_3ad["offsetTop"];
ret.y+=isNaN(m)?0:m;
_3ad=_3ad.offsetParent;
}while((_3ad!=_3ab)&&(_3ad!=null));
}else{
if(node["x"]&&node["y"]){
ret.x+=isNaN(node.x)?0:node.x;
ret.y+=isNaN(node.y)?0:node.y;
}
}
}
}
if(_3a2){
var _3b0=dojo.html.getScroll();
ret.y+=_3b0.top;
ret.x+=_3b0.left;
}
var _3b1=[dojo.html.getPaddingExtent,dojo.html.getBorderExtent,dojo.html.getMarginExtent];
if(_3a6>_3a7){
for(var i=_3a7;i<_3a6;++i){
ret.y+=_3b1[i](node,"top");
ret.x+=_3b1[i](node,"left");
}
}else{
if(_3a6<_3a7){
for(var i=_3a7;i>_3a6;--i){
ret.y-=_3b1[i-1](node,"top");
ret.x-=_3b1[i-1](node,"left");
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
dojo.html._sumPixelValues=function(node,_3b5,_3b6){
var _3b7=0;
for(var x=0;x<_3b5.length;x++){
_3b7+=dojo.html.getPixelValue(node,_3b5[x],_3b6);
}
return _3b7;
};
dojo.html.getMargin=function(node){
return {width:dojo.html._sumPixelValues(node,["margin-left","margin-right"],(dojo.html.getComputedStyle(node,"position")=="absolute")),height:dojo.html._sumPixelValues(node,["margin-top","margin-bottom"],(dojo.html.getComputedStyle(node,"position")=="absolute"))};
};
dojo.html.getBorder=function(node){
return {width:dojo.html.getBorderExtent(node,"left")+dojo.html.getBorderExtent(node,"right"),height:dojo.html.getBorderExtent(node,"top")+dojo.html.getBorderExtent(node,"bottom")};
};
dojo.html.getBorderExtent=function(node,side){
return (dojo.html.getStyle(node,"border-"+side+"-style")=="none"?0:dojo.html.getPixelValue(node,"border-"+side+"-width"));
};
dojo.html.getMarginExtent=function(node,side){
return dojo.html._sumPixelValues(node,["margin-"+side],dojo.html.isPositionAbsolute(node));
};
dojo.html.getPaddingExtent=function(node,side){
return dojo.html._sumPixelValues(node,["padding-"+side],true);
};
dojo.html.getPadding=function(node){
return {width:dojo.html._sumPixelValues(node,["padding-left","padding-right"],true),height:dojo.html._sumPixelValues(node,["padding-top","padding-bottom"],true)};
};
dojo.html.getPadBorder=function(node){
var pad=dojo.html.getPadding(node);
var _3c4=dojo.html.getBorder(node);
return {width:pad.width+_3c4.width,height:pad.height+_3c4.height};
};
dojo.html.getBoxSizing=function(node){
var h=dojo.render.html;
var bs=dojo.html.boxSizing;
if((h.ie)||(h.opera)){
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
var _3c9=dojo.html.getStyle(node,"-moz-box-sizing");
if(!_3c9){
_3c9=dojo.html.getStyle(node,"box-sizing");
}
return (_3c9?_3c9:bs.CONTENT_BOX);
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
var _3ce=dojo.html.getBorder(node);
return {width:box.width-_3ce.width,height:box.height-_3ce.height};
};
dojo.html.getContentBox=function(node){
node=dojo.byId(node);
var _3d0=dojo.html.getPadBorder(node);
return {width:node.offsetWidth-_3d0.width,height:node.offsetHeight-_3d0.height};
};
dojo.html.setContentBox=function(node,args){
node=dojo.byId(node);
var _3d3=0;
var _3d4=0;
var isbb=dojo.html.isBorderBox(node);
var _3d6=(isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var ret={};
if(typeof args.width!="undefined"){
_3d3=args.width+_3d6.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_3d3);
}
if(typeof args.height!="undefined"){
_3d4=args.height+_3d6.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_3d4);
}
return ret;
};
dojo.html.getMarginBox=function(node){
var _3d9=dojo.html.getBorderBox(node);
var _3da=dojo.html.getMargin(node);
return {width:_3d9.width+_3da.width,height:_3d9.height+_3da.height};
};
dojo.html.setMarginBox=function(node,args){
node=dojo.byId(node);
var _3dd=0;
var _3de=0;
var isbb=dojo.html.isBorderBox(node);
var _3e0=(!isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var _3e1=dojo.html.getMargin(node);
var ret={};
if(typeof args.width!="undefined"){
_3dd=args.width-_3e0.width;
_3dd-=_3e1.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_3dd);
}
if(typeof args.height!="undefined"){
_3de=args.height-_3e0.height;
_3de-=_3e1.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_3de);
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
dojo.html.toCoordinateObject=dojo.html.toCoordinateArray=function(_3e6,_3e7,_3e8){
if(_3e6 instanceof Array||typeof _3e6=="array"){
dojo.deprecated("dojo.html.toCoordinateArray","use dojo.html.toCoordinateObject({left: , top: , width: , height: }) instead","0.5");
while(_3e6.length<4){
_3e6.push(0);
}
while(_3e6.length>4){
_3e6.pop();
}
var ret={left:_3e6[0],top:_3e6[1],width:_3e6[2],height:_3e6[3]};
}else{
if(!_3e6.nodeType&&!(_3e6 instanceof String||typeof _3e6=="string")&&("width" in _3e6||"height" in _3e6||"left" in _3e6||"x" in _3e6||"top" in _3e6||"y" in _3e6)){
var ret={left:_3e6.left||_3e6.x||0,top:_3e6.top||_3e6.y||0,width:_3e6.width||0,height:_3e6.height||0};
}else{
var node=dojo.byId(_3e6);
var pos=dojo.html.abs(node,_3e7,_3e8);
var _3ec=dojo.html.getMarginBox(node);
var ret={left:pos.left,top:pos.top,width:_3ec.width,height:_3ec.height};
}
}
ret.x=ret.left;
ret.y=ret.top;
return ret;
};
dojo.html.setMarginBoxWidth=dojo.html.setOuterWidth=function(node,_3ee){
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
dojo.html.getTotalOffset=function(node,type,_3f1){
return dojo.html._callDeprecated("getTotalOffset","getAbsolutePosition",arguments,null,type);
};
dojo.html.getAbsoluteX=function(node,_3f3){
return dojo.html._callDeprecated("getAbsoluteX","getAbsolutePosition",arguments,null,"x");
};
dojo.html.getAbsoluteY=function(node,_3f5){
return dojo.html._callDeprecated("getAbsoluteY","getAbsolutePosition",arguments,null,"y");
};
dojo.html.totalOffsetLeft=function(node,_3f7){
return dojo.html._callDeprecated("totalOffsetLeft","getAbsolutePosition",arguments,null,"left");
};
dojo.html.totalOffsetTop=function(node,_3f9){
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
dojo.html.setContentBoxWidth=dojo.html.setContentWidth=function(node,_403){
return dojo.html._callDeprecated("setContentBoxWidth","setContentBox",arguments,"width");
};
dojo.html.setContentBoxHeight=dojo.html.setContentHeight=function(node,_405){
return dojo.html._callDeprecated("setContentBoxHeight","setContentBox",arguments,"height");
};
dojo.provide("dojo.dnd.HtmlDragManager");
dojo.declare("dojo.dnd.HtmlDragManager",dojo.dnd.DragManager,{disabled:false,nestedTargets:false,mouseDownTimer:null,dsCounter:0,dsPrefix:"dojoDragSource",dropTargetDimensions:[],currentDropTarget:null,previousDropTarget:null,_dragTriggered:false,selectedSources:[],dragObjects:[],currentX:null,currentY:null,lastX:null,lastY:null,mouseDownX:null,mouseDownY:null,threshold:7,dropAcceptable:false,cancelEvent:function(e){
e.stopPropagation();
e.preventDefault();
},registerDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _409=dp+"Idx_"+(this.dsCounter++);
ds.dragSourceId=_409;
this.dragSources[_409]=ds;
ds.domNode.setAttribute(dp,_409);
if(dojo.render.html.ie){
dojo.event.browser.addListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},unregisterDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _40c=ds.dragSourceId;
delete ds.dragSourceId;
delete this.dragSources[_40c];
ds.domNode.setAttribute(dp,null);
if(dojo.render.html.ie){
dojo.event.browser.removeListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},registerDropTarget:function(dt){
this.dropTargets.push(dt);
},unregisterDropTarget:function(dt){
var _40f=dojo.lang.find(this.dropTargets,dt,true);
if(_40f>=0){
this.dropTargets.splice(_40f,1);
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
var _415=e.target.nodeType==dojo.html.TEXT_NODE?e.target.parentNode:e.target;
if(dojo.html.isTag(_415,"button","textarea","input","select","option")){
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
},onMouseUp:function(e,_418){
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
dojo.lang.forEach(this.dragObjects,function(_419){
var ret=null;
if(!_419){
return;
}
if(this.currentDropTarget){
e.dragObject=_419;
var ce=this.currentDropTarget.domNode.childNodes;
if(ce.length>0){
e.dropTarget=ce[0];
while(e.dropTarget==_419.domNode){
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
_419.dragSource.onDragEnd(e);
}
catch(err){
var _41c={};
for(var i in e){
if(i=="type"){
_41c.type="mouseup";
continue;
}
_41c[i]=e[i];
}
_419.dragSource.onDragEnd(_41c);
}
},function(){
_419.onDragEnd(e);
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
dojo.lang.forEach(this.dropTargets,function(_425){
var tn=_425.domNode;
if(!tn||dojo.lang.find(_425.acceptedTypes,this.dragSource.type)<0){
return;
}
var abs=dojo.html.getAbsolutePosition(tn,true);
var bb=dojo.html.getBorderBox(tn);
this.dropTargetDimensions.push([[abs.x,abs.y],[abs.x+bb.width,abs.y+bb.height],_425]);
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
dojo.lang.forEach(this.selectedSources,function(_42c){
if(!_42c){
return;
}
var tdo=_42c.onDragStart(e);
if(tdo){
tdo.onDragStart(e);
tdo.dragOffset.y+=dy;
tdo.dragOffset.x+=dx;
tdo.dragSource=_42c;
this.dragObjects.push(tdo);
}
},this);
this.previousDropTarget=null;
this.cacheTargetLocations();
}
dojo.lang.forEach(this.dragObjects,function(_42e){
if(_42e){
_42e.onDragMove(e);
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
var _431=this.findBestTarget(e);
if(_431.target===null){
if(this.currentDropTarget){
this.currentDropTarget.onDragOut(e);
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget=null;
}
this.dropAcceptable=false;
return;
}
if(this.currentDropTarget!==_431.target){
if(this.currentDropTarget){
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget.onDragOut(e);
}
this.currentDropTarget=_431.target;
e.dragObjects=this.dragObjects;
this.dropAcceptable=this.currentDropTarget.onDragOver(e);
}else{
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}
}
},findBestTarget:function(e){
var _433=this;
var _434=new Object();
_434.target=null;
_434.points=null;
dojo.lang.every(this.dropTargetDimensions,function(_435){
if(!_433.isInsideBox(e,_435)){
return true;
}
_434.target=_435[2];
_434.points=_435;
return Boolean(_433.nestedTargets);
});
return _434;
},isInsideBox:function(e,_437){
if((e.pageX>_437[0][0])&&(e.pageX<_437[1][0])&&(e.pageY>_437[0][1])&&(e.pageY<_437[1][1])){
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
dojo.html.getElementWindow=function(_43c){
return dojo.html.getDocumentWindow(_43c.ownerDocument);
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
dojo.html.gravity=function(node,e){
node=dojo.byId(node);
var _444=dojo.html.getCursorPosition(e);
with(dojo.html){
var _445=getAbsolutePosition(node,true);
var bb=getBorderBox(node);
var _447=_445.x+(bb.width/2);
var _448=_445.y+(bb.height/2);
}
with(dojo.html.gravity){
return ((_444.x<_447?WEST:EAST)|(_444.y<_448?NORTH:SOUTH));
}
};
dojo.html.gravity.NORTH=1;
dojo.html.gravity.SOUTH=1<<1;
dojo.html.gravity.EAST=1<<2;
dojo.html.gravity.WEST=1<<3;
dojo.html.overElement=function(_449,e){
_449=dojo.byId(_449);
var _44b=dojo.html.getCursorPosition(e);
var bb=dojo.html.getBorderBox(_449);
var _44d=dojo.html.getAbsolutePosition(_449,true,dojo.html.boxSizing.BORDER_BOX);
var top=_44d.y;
var _44f=top+bb.height;
var left=_44d.x;
var _451=left+bb.width;
return (_44b.x>=left&&_44b.x<=_451&&_44b.y>=top&&_44b.y<=_44f);
};
dojo.html.renderedTextContent=function(node){
node=dojo.byId(node);
var _453="";
if(node==null){
return _453;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
var _455="unknown";
try{
_455=dojo.html.getStyle(node.childNodes[i],"display");
}
catch(E){
}
switch(_455){
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
_453+="\n";
_453+=dojo.html.renderedTextContent(node.childNodes[i]);
_453+="\n";
break;
case "none":
break;
default:
if(node.childNodes[i].tagName&&node.childNodes[i].tagName.toLowerCase()=="br"){
_453+="\n";
}else{
_453+=dojo.html.renderedTextContent(node.childNodes[i]);
}
break;
}
break;
case 3:
case 2:
case 4:
var text=node.childNodes[i].nodeValue;
var _457="unknown";
try{
_457=dojo.html.getStyle(node,"text-transform");
}
catch(E){
}
switch(_457){
case "capitalize":
var _458=text.split(" ");
for(var i=0;i<_458.length;i++){
_458[i]=_458[i].charAt(0).toUpperCase()+_458[i].substring(1);
}
text=_458.join(" ");
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
switch(_457){
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
if(/\s$/.test(_453)){
text.replace(/^\s/,"");
}
break;
}
_453+=text;
break;
default:
break;
}
}
return _453;
};
dojo.html.createNodesFromText=function(txt,trim){
if(trim){
txt=txt.replace(/^\s+|\s+$/g,"");
}
var tn=dojo.doc().createElement("div");
tn.style.visibility="hidden";
dojo.body().appendChild(tn);
var _45c="none";
if((/^<t[dh][\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody><tr>"+txt+"</tr></tbody></table>";
_45c="cell";
}else{
if((/^<tr[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody>"+txt+"</tbody></table>";
_45c="row";
}else{
if((/^<(thead|tbody|tfoot)[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table>"+txt+"</table>";
_45c="section";
}
}
}
tn.innerHTML=txt;
if(tn["normalize"]){
tn.normalize();
}
var _45d=null;
switch(_45c){
case "cell":
_45d=tn.getElementsByTagName("tr")[0];
break;
case "row":
_45d=tn.getElementsByTagName("tbody")[0];
break;
case "section":
_45d=tn.getElementsByTagName("table")[0];
break;
default:
_45d=tn;
break;
}
var _45e=[];
for(var x=0;x<_45d.childNodes.length;x++){
_45e.push(_45d.childNodes[x].cloneNode(true));
}
tn.style.display="none";
dojo.body().removeChild(tn);
return _45e;
};
dojo.html.placeOnScreen=function(node,_461,_462,_463,_464,_465,_466){
if(_461 instanceof Array||typeof _461=="array"){
_466=_465;
_465=_464;
_464=_463;
_463=_462;
_462=_461[1];
_461=_461[0];
}
if(_465 instanceof String||typeof _465=="string"){
_465=_465.split(",");
}
if(!isNaN(_463)){
_463=[Number(_463),Number(_463)];
}else{
if(!(_463 instanceof Array||typeof _463=="array")){
_463=[0,0];
}
}
var _467=dojo.html.getScroll().offset;
var view=dojo.html.getViewport();
node=dojo.byId(node);
var _469=node.style.display;
node.style.display="";
var bb=dojo.html.getBorderBox(node);
var w=bb.width;
var h=bb.height;
node.style.display=_469;
if(!(_465 instanceof Array||typeof _465=="array")){
_465=["TL"];
}
var _46d,_46e,_46f=Infinity,_470;
for(var _471=0;_471<_465.length;++_471){
var _472=_465[_471];
var _473=true;
var tryX=_461-(_472.charAt(1)=="L"?0:w)+_463[0]*(_472.charAt(1)=="L"?1:-1);
var tryY=_462-(_472.charAt(0)=="T"?0:h)+_463[1]*(_472.charAt(0)=="T"?1:-1);
if(_464){
tryX-=_467.x;
tryY-=_467.y;
}
if(tryX<0){
tryX=0;
_473=false;
}
if(tryY<0){
tryY=0;
_473=false;
}
var x=tryX+w;
if(x>view.width){
x=view.width-w;
_473=false;
}else{
x=tryX;
}
x=Math.max(_463[0],x)+_467.x;
var y=tryY+h;
if(y>view.height){
y=view.height-h;
_473=false;
}else{
y=tryY;
}
y=Math.max(_463[1],y)+_467.y;
if(_473){
_46d=x;
_46e=y;
_46f=0;
_470=_472;
break;
}else{
var dist=Math.pow(x-tryX-_467.x,2)+Math.pow(y-tryY-_467.y,2);
if(_46f>dist){
_46f=dist;
_46d=x;
_46e=y;
_470=_472;
}
}
}
if(!_466){
node.style.left=_46d+"px";
node.style.top=_46e+"px";
}
return {left:_46d,top:_46e,x:_46d,y:_46e,dist:_46f,corner:_470};
};
dojo.html.placeOnScreenPoint=function(node,_47a,_47b,_47c,_47d){
dojo.deprecated("dojo.html.placeOnScreenPoint","use dojo.html.placeOnScreen() instead","0.5");
return dojo.html.placeOnScreen(node,_47a,_47b,_47c,_47d,["TL","TR","BL","BR"]);
};
dojo.html.placeOnScreenAroundElement=function(node,_47f,_480,_481,_482,_483){
var best,_485=Infinity;
_47f=dojo.byId(_47f);
var _486=_47f.style.display;
_47f.style.display="";
var mb=dojo.html.getElementBox(_47f,_481);
var _488=mb.width;
var _489=mb.height;
var _48a=dojo.html.getAbsolutePosition(_47f,true,_481);
_47f.style.display=_486;
for(var _48b in _482){
var pos,_48d,_48e;
var _48f=_482[_48b];
_48d=_48a.x+(_48b.charAt(1)=="L"?0:_488);
_48e=_48a.y+(_48b.charAt(0)=="T"?0:_489);
pos=dojo.html.placeOnScreen(node,_48d,_48e,_480,true,_48f,true);
if(pos.dist==0){
best=pos;
break;
}else{
if(_485>pos.dist){
_485=pos.dist;
best=pos;
}
}
}
if(!_483){
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
if(dojo.html.getBorderBox(node.parentNode).height<node.parentNode.scrollHeight){
node.scrollIntoView(false);
}
}else{
if(dojo.render.html.mozilla){
node.scrollIntoView(false);
}else{
var _491=node.parentNode;
var _492=_491.scrollTop+dojo.html.getBorderBox(_491).height;
var _493=node.offsetTop+dojo.html.getMarginBox(node).height;
if(_492<_493){
_491.scrollTop+=(_493-_492);
}else{
if(_491.scrollTop>node.offsetTop){
_491.scrollTop-=(_491.scrollTop-node.offsetTop);
}
}
}
}
};
dojo.provide("dojo.html.selection");
dojo.html.selectionType={NONE:0,TEXT:1,CONTROL:2};
dojo.html.clearSelection=function(){
var _494=dojo.global();
var _495=dojo.doc();
try{
if(_494["getSelection"]){
if(dojo.render.html.safari){
_494.getSelection().collapse();
}else{
_494.getSelection().removeAllRanges();
}
}else{
if(_495.selection){
if(_495.selection.empty){
_495.selection.empty();
}else{
if(_495.selection.clear){
_495.selection.clear();
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
dojo.html.disableSelection=function(_496){
_496=dojo.byId(_496)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_496.style.MozUserSelect="none";
}else{
if(h.safari){
_496.style.KhtmlUserSelect="none";
}else{
if(h.ie){
_496.unselectable="on";
}else{
return false;
}
}
}
return true;
};
dojo.html.enableSelection=function(_498){
_498=dojo.byId(_498)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_498.style.MozUserSelect="";
}else{
if(h.safari){
_498.style.KhtmlUserSelect="";
}else{
if(h.ie){
_498.unselectable="off";
}else{
return false;
}
}
}
return true;
};
dojo.html.selectElement=function(_49a){
dojo.deprecated("dojo.html.selectElement","replaced by dojo.html.selection.selectElementChildren",0.5);
};
dojo.html.selectInputText=function(_49b){
var _49c=dojo.global();
var _49d=dojo.doc();
_49b=dojo.byId(_49b);
if(_49d["selection"]&&dojo.body()["createTextRange"]){
var _49e=_49b.createTextRange();
_49e.moveStart("character",0);
_49e.moveEnd("character",_49b.value.length);
_49e.select();
}else{
if(_49c["getSelection"]){
var _49f=_49c.getSelection();
_49b.setSelectionRange(0,_49b.value.length);
}
}
_49b.focus();
};
dojo.html.isSelectionCollapsed=function(){
dojo.deprecated("dojo.html.isSelectionCollapsed","replaced by dojo.html.selection.isCollapsed",0.5);
return dojo.html.selection.isCollapsed();
};
dojo.lang.mixin(dojo.html.selection,{getType:function(){
if(dojo.doc()["selection"]){
return dojo.html.selectionType[dojo.doc().selection.type.toUpperCase()];
}else{
var _4a0=dojo.html.selectionType.TEXT;
var oSel;
try{
oSel=dojo.global().getSelection();
}
catch(e){
}
if(oSel&&oSel.rangeCount==1){
var _4a2=oSel.getRangeAt(0);
if(_4a2.startContainer==_4a2.endContainer&&(_4a2.endOffset-_4a2.startOffset)==1&&_4a2.startContainer.nodeType!=dojo.dom.TEXT_NODE){
_4a0=dojo.html.selectionType.CONTROL;
}
}
return _4a0;
}
},isCollapsed:function(){
var _4a3=dojo.global();
var _4a4=dojo.doc();
if(_4a4["selection"]){
return _4a4.selection.createRange().text=="";
}else{
if(_4a3["getSelection"]){
var _4a5=_4a3.getSelection();
if(dojo.lang.isString(_4a5)){
return _4a5=="";
}else{
return _4a5.isCollapsed||_4a5.toString()=="";
}
}
}
},getSelectedElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
if(dojo.doc()["selection"]){
var _4a6=dojo.doc().selection.createRange();
if(_4a6&&_4a6.item){
return dojo.doc().selection.createRange().item(0);
}
}else{
var _4a7=dojo.global().getSelection();
return _4a7.anchorNode.childNodes[_4a7.anchorOffset];
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
var _4a9=dojo.global().getSelection();
if(_4a9){
var node=_4a9.anchorNode;
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
var _4ab=dojo.global().getSelection();
if(_4ab){
return _4ab.toString();
}
}
},getSelectedHtml:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().htmlText;
}else{
var _4ac=dojo.global().getSelection();
if(_4ac&&_4ac.rangeCount){
var frag=_4ac.getRangeAt(0).cloneContents();
var div=document.createElement("div");
div.appendChild(frag);
return div.innerHTML;
}
return null;
}
},hasAncestorElement:function(_4af){
return (dojo.html.selection.getAncestorElement.apply(this,arguments)!=null);
},getAncestorElement:function(_4b0){
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
},selectElement:function(_4b5){
var _4b6=dojo.global();
var _4b7=dojo.doc();
_4b5=dojo.byId(_4b5);
if(_4b7.selection&&dojo.body().createTextRange){
try{
var _4b8=dojo.body().createControlRange();
_4b8.addElement(_4b5);
_4b8.select();
}
catch(e){
dojo.html.selection.selectElementChildren(_4b5);
}
}else{
if(_4b6["getSelection"]){
var _4b9=_4b6.getSelection();
if(_4b9["removeAllRanges"]){
var _4b8=_4b7.createRange();
_4b8.selectNode(_4b5);
_4b9.removeAllRanges();
_4b9.addRange(_4b8);
}
}
}
},selectElementChildren:function(_4ba){
var _4bb=dojo.global();
var _4bc=dojo.doc();
_4ba=dojo.byId(_4ba);
if(_4bc.selection&&dojo.body().createTextRange){
var _4bd=dojo.body().createTextRange();
_4bd.moveToElementText(_4ba);
_4bd.select();
}else{
if(_4bb["getSelection"]){
var _4be=_4bb.getSelection();
if(_4be["setBaseAndExtent"]){
_4be.setBaseAndExtent(_4ba,0,_4ba,_4ba.innerText.length-1);
}else{
if(_4be["selectAllChildren"]){
_4be.selectAllChildren(_4ba);
}
}
}
}
},getBookmark:function(){
var _4bf;
var _4c0=dojo.doc();
if(_4c0["selection"]){
var _4c1=_4c0.selection.createRange();
_4bf=_4c1.getBookmark();
}else{
var _4c2;
try{
_4c2=dojo.global().getSelection();
}
catch(e){
}
if(_4c2){
var _4c1=_4c2.getRangeAt(0);
_4bf=_4c1.cloneRange();
}else{
dojo.debug("No idea how to store the current selection for this browser!");
}
}
return _4bf;
},moveToBookmark:function(_4c3){
var _4c4=dojo.doc();
if(_4c4["selection"]){
var _4c5=_4c4.selection.createRange();
_4c5.moveToBookmark(_4c3);
_4c5.select();
}else{
var _4c6;
try{
_4c6=dojo.global().getSelection();
}
catch(e){
}
if(_4c6&&_4c6["removeAllRanges"]){
_4c6.removeAllRanges();
_4c6.addRange(_4c3);
}else{
dojo.debug("No idea how to restore selection for this browser!");
}
}
},collapse:function(_4c7){
if(dojo.global()["getSelection"]){
var _4c8=dojo.global().getSelection();
if(_4c8.removeAllRanges){
if(_4c7){
_4c8.collapseToStart();
}else{
_4c8.collapseToEnd();
}
}else{
dojo.global().getSelection().collapse(_4c7);
}
}else{
if(dojo.doc().selection){
var _4c9=dojo.doc().selection.createRange();
_4c9.collapse(_4c7);
_4c9.select();
}
}
},remove:function(){
if(dojo.doc().selection){
var _4ca=dojo.doc().selection;
if(_4ca.type.toUpperCase()!="NONE"){
_4ca.clear();
}
return _4ca;
}else{
var _4ca=dojo.global().getSelection();
for(var i=0;i<_4ca.rangeCount;i++){
_4ca.getRangeAt(i).deleteContents();
}
return _4ca;
}
}});
dojo.provide("dojo.html.iframe");
dojo.html.iframeContentWindow=function(_4cc){
var win=dojo.html.getDocumentWindow(dojo.html.iframeContentDocument(_4cc))||dojo.html.iframeContentDocument(_4cc).__parent__||(_4cc.name&&document.frames[_4cc.name])||null;
return win;
};
dojo.html.iframeContentDocument=function(_4ce){
var doc=_4ce.contentDocument||((_4ce.contentWindow)&&(_4ce.contentWindow.document))||((_4ce.name)&&(document.frames[_4ce.name])&&(document.frames[_4ce.name].document))||null;
return doc;
};
dojo.html.BackgroundIframe=function(node){
if(dojo.render.html.ie55||dojo.render.html.ie60){
var html="<iframe src='javascript:false'"+"' style='position: absolute; left: 0px; top: 0px; width: 100%; height: 100%;"+"z-index: -1; filter:Alpha(Opacity=\"0\");' "+">";
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
var _4d2=dojo.html.getMarginBox(this.domNode);
if(_4d2.width==0||_4d2.height==0){
dojo.lang.setTimeout(this,this.onResized,100);
return;
}
this.iframe.style.width=_4d2.width+"px";
this.iframe.style.height=_4d2.height+"px";
}
},size:function(node){
if(!this.iframe){
return;
}
var _4d4=dojo.html.toCoordinateObject(node,true,dojo.html.boxSizing.BORDER_BOX);
this.iframe.style.width=_4d4.width+"px";
this.iframe.style.height=_4d4.height+"px";
this.iframe.style.left=_4d4.left+"px";
this.iframe.style.top=_4d4.top+"px";
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
if(!this.iframe){
return;
}
this.iframe.style.display="block";
},hide:function(){
if(!this.iframe){
return;
}
this.iframe.style.display="none";
},remove:function(){
dojo.html.removeNode(this.iframe);
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
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_4dc){
if(_4dc){
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
},blend:function(_4dd,_4de){
var rgb=null;
if(dojo.lang.isArray(_4dd)){
rgb=_4dd;
}else{
if(_4dd instanceof dojo.gfx.color.Color){
rgb=_4dd.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_4dd).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_4de);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_4e2){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_4e2);
}
if(!_4e2){
_4e2=0;
}
_4e2=Math.min(Math.max(-1,_4e2),1);
_4e2=((_4e2+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_4e2));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_4e7){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_4e7));
};
dojo.gfx.color.extractRGB=function(_4e8){
var hex="0123456789abcdef";
_4e8=_4e8.toLowerCase();
if(_4e8.indexOf("rgb")==0){
var _4ea=_4e8.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_4ea.splice(1,3);
return ret;
}else{
var _4ec=dojo.gfx.color.hex2rgb(_4e8);
if(_4ec){
return _4ec;
}else{
return dojo.gfx.color.named[_4e8]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _4ee="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_4ee+"]","g"),"")!=""){
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
rgb[i]=_4ee.indexOf(rgb[i].charAt(0))*16+_4ee.indexOf(rgb[i].charAt(1));
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
dojo.lfx.Line=function(_4f7,end){
this.start=_4f7;
this.end=end;
if(dojo.lang.isArray(_4f7)){
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
var diff=end-_4f7;
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
dojo.lang.extend(dojo.lfx.IAnimation,{curve:null,duration:1000,easing:null,repeatCount:0,rate:25,handler:null,beforeBegin:null,onBegin:null,onAnimate:null,onEnd:null,onPlay:null,onPause:null,onStop:null,play:null,pause:null,stop:null,connect:function(evt,_506,_507){
if(!_507){
_507=_506;
_506=this;
}
_507=dojo.lang.hitch(_506,_507);
var _508=this[evt]||function(){
};
this[evt]=function(){
var ret=_508.apply(this,arguments);
_507.apply(this,arguments);
return ret;
};
return this;
},fire:function(evt,args){
if(this[evt]){
this[evt].apply(this,(args||[]));
}
return this;
},repeat:function(_50c){
this.repeatCount=_50c;
return this;
},_active:false,_paused:false});
dojo.lfx.Animation=function(_50d,_50e,_50f,_510,_511,rate){
dojo.lfx.IAnimation.call(this);
if(dojo.lang.isNumber(_50d)||(!_50d&&_50e.getValue)){
rate=_511;
_511=_510;
_510=_50f;
_50f=_50e;
_50e=_50d;
_50d=null;
}else{
if(_50d.getValue||dojo.lang.isArray(_50d)){
rate=_510;
_511=_50f;
_510=_50e;
_50f=_50d;
_50e=null;
_50d=null;
}
}
if(dojo.lang.isArray(_50f)){
this.curve=new dojo.lfx.Line(_50f[0],_50f[1]);
}else{
this.curve=_50f;
}
if(_50e!=null&&_50e>0){
this.duration=_50e;
}
if(_511){
this.repeatCount=_511;
}
if(rate){
this.rate=rate;
}
if(_50d){
dojo.lang.forEach(["handler","beforeBegin","onBegin","onEnd","onPlay","onStop","onAnimate"],function(item){
if(_50d[item]){
this.connect(item,_50d[item]);
}
},this);
}
if(_510&&dojo.lang.isFunction(_510)){
this.easing=_510;
}
};
dojo.inherits(dojo.lfx.Animation,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Animation,{_startTime:null,_endTime:null,_timer:null,_percent:0,_startRepeatCount:0,play:function(_514,_515){
if(_515){
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
if(_514>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_515);
}),_514);
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
var _517=this.curve.getValue(step);
if(this._percent==0){
if(!this._startRepeatCount){
this._startRepeatCount=this.repeatCount;
}
this.fire("handler",["begin",_517]);
this.fire("onBegin",[_517]);
}
this.fire("handler",["play",_517]);
this.fire("onPlay",[_517]);
this._cycle();
return this;
},pause:function(){
clearTimeout(this._timer);
if(!this._active){
return this;
}
this._paused=true;
var _518=this.curve.getValue(this._percent/100);
this.fire("handler",["pause",_518]);
this.fire("onPause",[_518]);
return this;
},gotoPercent:function(pct,_51a){
clearTimeout(this._timer);
this._active=true;
this._paused=true;
this._percent=pct;
if(_51a){
this.play();
}
return this;
},stop:function(_51b){
clearTimeout(this._timer);
var step=this._percent/100;
if(_51b){
step=1;
}
var _51d=this.curve.getValue(step);
this.fire("handler",["stop",_51d]);
this.fire("onStop",[_51d]);
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
var _520=this.curve.getValue(step);
this.fire("handler",["animate",_520]);
this.fire("onAnimate",[_520]);
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
dojo.lfx.Combine=function(_521){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._animsEnded=0;
var _522=arguments;
if(_522.length==1&&(dojo.lang.isArray(_522[0])||dojo.lang.isArrayLike(_522[0]))){
_522=_522[0];
}
dojo.lang.forEach(_522,function(anim){
this._anims.push(anim);
anim.connect("onEnd",dojo.lang.hitch(this,"_onAnimsEnded"));
},this);
};
dojo.inherits(dojo.lfx.Combine,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Combine,{_animsEnded:0,play:function(_524,_525){
if(!this._anims.length){
return this;
}
this.fire("beforeBegin");
if(_524>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_525);
}),_524);
return this;
}
if(_525||this._anims[0].percent==0){
this.fire("onBegin");
}
this.fire("onPlay");
this._animsCall("play",null,_525);
return this;
},pause:function(){
this.fire("onPause");
this._animsCall("pause");
return this;
},stop:function(_526){
this.fire("onStop");
this._animsCall("stop",_526);
return this;
},_onAnimsEnded:function(){
this._animsEnded++;
if(this._animsEnded>=this._anims.length){
this.fire("onEnd");
}
return this;
},_animsCall:function(_527){
var args=[];
if(arguments.length>1){
for(var i=1;i<arguments.length;i++){
args.push(arguments[i]);
}
}
var _52a=this;
dojo.lang.forEach(this._anims,function(anim){
anim[_527](args);
},_52a);
return this;
}});
dojo.lfx.Chain=function(_52c){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._currAnim=-1;
var _52d=arguments;
if(_52d.length==1&&(dojo.lang.isArray(_52d[0])||dojo.lang.isArrayLike(_52d[0]))){
_52d=_52d[0];
}
var _52e=this;
dojo.lang.forEach(_52d,function(anim,i,_531){
this._anims.push(anim);
if(i<_531.length-1){
anim.connect("onEnd",dojo.lang.hitch(this,"_playNext"));
}else{
anim.connect("onEnd",dojo.lang.hitch(this,function(){
this.fire("onEnd");
}));
}
},this);
};
dojo.inherits(dojo.lfx.Chain,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Chain,{_currAnim:-1,play:function(_532,_533){
if(!this._anims.length){
return this;
}
if(_533||!this._anims[this._currAnim]){
this._currAnim=0;
}
var _534=this._anims[this._currAnim];
this.fire("beforeBegin");
if(_532>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_533);
}),_532);
return this;
}
if(_534){
if(this._currAnim==0){
this.fire("handler",["begin",this._currAnim]);
this.fire("onBegin",[this._currAnim]);
}
this.fire("onPlay",[this._currAnim]);
_534.play(null,_533);
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
var _535=this._anims[this._currAnim];
if(_535){
if(!_535._active||_535._paused){
this.play();
}else{
this.pause();
}
}
return this;
},stop:function(){
var _536=this._anims[this._currAnim];
if(_536){
_536.stop();
this.fire("onStop",[this._currAnim]);
}
return _536;
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
dojo.lfx.combine=function(_537){
var _538=arguments;
if(dojo.lang.isArray(arguments[0])){
_538=arguments[0];
}
if(_538.length==1){
return _538[0];
}
return new dojo.lfx.Combine(_538);
};
dojo.lfx.chain=function(_539){
var _53a=arguments;
if(dojo.lang.isArray(arguments[0])){
_53a=arguments[0];
}
if(_53a.length==1){
return _53a[0];
}
return new dojo.lfx.Chain(_53a);
};
dojo.provide("dojo.html.color");
dojo.html.getBackgroundColor=function(node){
node=dojo.byId(node);
var _53c;
do{
_53c=dojo.html.getStyle(node,"background-color");
if(_53c.toLowerCase()=="rgba(0, 0, 0, 0)"){
_53c="transparent";
}
if(node==document.getElementsByTagName("body")[0]){
node=null;
break;
}
node=node.parentNode;
}while(node&&dojo.lang.inArray(["transparent",""],_53c));
if(_53c=="transparent"){
_53c=[255,255,255,0];
}else{
_53c=dojo.gfx.color.extractRGB(_53c);
}
return _53c;
};
dojo.provide("dojo.lfx.html");
dojo.lfx.html._byId=function(_53d){
if(!_53d){
return [];
}
if(dojo.lang.isArrayLike(_53d)){
if(!_53d.alreadyChecked){
var n=[];
dojo.lang.forEach(_53d,function(node){
n.push(dojo.byId(node));
});
n.alreadyChecked=true;
return n;
}else{
return _53d;
}
}else{
var n=[];
n.push(dojo.byId(_53d));
n.alreadyChecked=true;
return n;
}
};
dojo.lfx.html.propertyAnimation=function(_540,_541,_542,_543,_544){
_540=dojo.lfx.html._byId(_540);
var _545={"propertyMap":_541,"nodes":_540,"duration":_542,"easing":_543||dojo.lfx.easeDefault};
var _546=function(args){
if(args.nodes.length==1){
var pm=args.propertyMap;
if(!dojo.lang.isArray(args.propertyMap)){
var parr=[];
for(var _54a in pm){
pm[_54a].property=_54a;
parr.push(pm[_54a]);
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
var _54c=function(_54d){
var _54e=[];
dojo.lang.forEach(_54d,function(c){
_54e.push(Math.round(c));
});
return _54e;
};
var _550=function(n,_552){
n=dojo.byId(n);
if(!n||!n.style){
return;
}
for(var s in _552){
if(s=="opacity"){
dojo.html.setOpacity(n,_552[s]);
}else{
n.style[s]=_552[s];
}
}
};
var _554=function(_555){
this._properties=_555;
this.diffs=new Array(_555.length);
dojo.lang.forEach(_555,function(prop,i){
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
var _55c=null;
if(dojo.lang.isArray(prop.start)){
}else{
if(prop.start instanceof dojo.gfx.color.Color){
_55c=(prop.units||"rgb")+"(";
for(var j=0;j<prop.startRgb.length;j++){
_55c+=Math.round(((prop.endRgb[j]-prop.startRgb[j])*n)+prop.startRgb[j])+(j<prop.startRgb.length-1?",":"");
}
_55c+=")";
}else{
_55c=((this.diffs[i])*n)+prop.start+(prop.property!="opacity"?prop.units||"px":"");
}
}
ret[dojo.html.toCamelCase(prop.property)]=_55c;
},this);
return ret;
};
};
var anim=new dojo.lfx.Animation({beforeBegin:function(){
_546(_545);
anim.curve=new _554(_545.propertyMap);
},onAnimate:function(_55f){
dojo.lang.forEach(_545.nodes,function(node){
_550(node,_55f);
});
}},_545.duration,null,_545.easing);
if(_544){
for(var x in _544){
if(dojo.lang.isFunction(_544[x])){
anim.connect(x,anim,_544[x]);
}
}
}
return anim;
};
dojo.lfx.html._makeFadeable=function(_562){
var _563=function(node){
if(dojo.render.html.ie){
if((node.style.zoom.length==0)&&(dojo.html.getStyle(node,"zoom")=="normal")){
node.style.zoom="1";
}
if((node.style.width.length==0)&&(dojo.html.getStyle(node,"width")=="auto")){
node.style.width="auto";
}
}
};
if(dojo.lang.isArrayLike(_562)){
dojo.lang.forEach(_562,_563);
}else{
_563(_562);
}
};
dojo.lfx.html.fade=function(_565,_566,_567,_568,_569){
_565=dojo.lfx.html._byId(_565);
var _56a={property:"opacity"};
if(!dj_undef("start",_566)){
_56a.start=_566.start;
}else{
_56a.start=function(){
return dojo.html.getOpacity(_565[0]);
};
}
if(!dj_undef("end",_566)){
_56a.end=_566.end;
}else{
dojo.raise("dojo.lfx.html.fade needs an end value");
}
var anim=dojo.lfx.propertyAnimation(_565,[_56a],_567,_568);
anim.connect("beforeBegin",function(){
dojo.lfx.html._makeFadeable(_565);
});
if(_569){
anim.connect("onEnd",function(){
_569(_565,anim);
});
}
return anim;
};
dojo.lfx.html.fadeIn=function(_56c,_56d,_56e,_56f){
return dojo.lfx.html.fade(_56c,{end:1},_56d,_56e,_56f);
};
dojo.lfx.html.fadeOut=function(_570,_571,_572,_573){
return dojo.lfx.html.fade(_570,{end:0},_571,_572,_573);
};
dojo.lfx.html.fadeShow=function(_574,_575,_576,_577){
_574=dojo.lfx.html._byId(_574);
dojo.lang.forEach(_574,function(node){
dojo.html.setOpacity(node,0);
});
var anim=dojo.lfx.html.fadeIn(_574,_575,_576,_577);
anim.connect("beforeBegin",function(){
if(dojo.lang.isArrayLike(_574)){
dojo.lang.forEach(_574,dojo.html.show);
}else{
dojo.html.show(_574);
}
});
return anim;
};
dojo.lfx.html.fadeHide=function(_57a,_57b,_57c,_57d){
var anim=dojo.lfx.html.fadeOut(_57a,_57b,_57c,function(){
if(dojo.lang.isArrayLike(_57a)){
dojo.lang.forEach(_57a,dojo.html.hide);
}else{
dojo.html.hide(_57a);
}
if(_57d){
_57d(_57a,anim);
}
});
return anim;
};
dojo.lfx.html.wipeIn=function(_57f,_580,_581,_582){
_57f=dojo.lfx.html._byId(_57f);
var _583=[];
dojo.lang.forEach(_57f,function(node){
var _585={};
dojo.html.show(node);
var _586=dojo.html.getBorderBox(node).height;
dojo.html.hide(node);
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:1,end:function(){
return _586;
}}},_580,_581);
anim.connect("beforeBegin",function(){
_585.overflow=node.style.overflow;
_585.height=node.style.height;
with(node.style){
overflow="hidden";
_586="1px";
}
dojo.html.show(node);
});
anim.connect("onEnd",function(){
with(node.style){
overflow=_585.overflow;
_586=_585.height;
}
if(_582){
_582(node,anim);
}
});
_583.push(anim);
});
return dojo.lfx.combine(_583);
};
dojo.lfx.html.wipeOut=function(_588,_589,_58a,_58b){
_588=dojo.lfx.html._byId(_588);
var _58c=[];
dojo.lang.forEach(_588,function(node){
var _58e={};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:function(){
return dojo.html.getContentBox(node).height;
},end:1}},_589,_58a,{"beforeBegin":function(){
_58e.overflow=node.style.overflow;
_58e.height=node.style.height;
with(node.style){
overflow="hidden";
}
dojo.html.show(node);
},"onEnd":function(){
dojo.html.hide(node);
with(node.style){
overflow=_58e.overflow;
height=_58e.height;
}
if(_58b){
_58b(node,anim);
}
}});
_58c.push(anim);
});
return dojo.lfx.combine(_58c);
};
dojo.lfx.html.slideTo=function(_590,_591,_592,_593,_594){
_590=dojo.lfx.html._byId(_590);
var _595=[];
var _596=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_591)){
dojo.deprecated("dojo.lfx.html.slideTo(node, array)","use dojo.lfx.html.slideTo(node, {top: value, left: value});","0.5");
_591={top:_591[0],left:_591[1]};
}
dojo.lang.forEach(_590,function(node){
var top=null;
var left=null;
var init=(function(){
var _59b=node;
return function(){
var pos=_596(_59b,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_596(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_596(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_59b,true);
dojo.html.setStyleAttributes(_59b,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:(_591.top||0)},"left":{start:left,end:(_591.left||0)}},_592,_593,{"beforeBegin":init});
if(_594){
anim.connect("onEnd",function(){
_594(_590,anim);
});
}
_595.push(anim);
});
return dojo.lfx.combine(_595);
};
dojo.lfx.html.slideBy=function(_59f,_5a0,_5a1,_5a2,_5a3){
_59f=dojo.lfx.html._byId(_59f);
var _5a4=[];
var _5a5=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_5a0)){
dojo.deprecated("dojo.lfx.html.slideBy(node, array)","use dojo.lfx.html.slideBy(node, {top: value, left: value});","0.5");
_5a0={top:_5a0[0],left:_5a0[1]};
}
dojo.lang.forEach(_59f,function(node){
var top=null;
var left=null;
var init=(function(){
var _5aa=node;
return function(){
var pos=_5a5(_5aa,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_5a5(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_5a5(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_5aa,true);
dojo.html.setStyleAttributes(_5aa,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:top+(_5a0.top||0)},"left":{start:left,end:left+(_5a0.left||0)}},_5a1,_5a2).connect("beforeBegin",init);
if(_5a3){
anim.connect("onEnd",function(){
_5a3(_59f,anim);
});
}
_5a4.push(anim);
});
return dojo.lfx.combine(_5a4);
};
dojo.lfx.html.explode=function(_5ae,_5af,_5b0,_5b1,_5b2){
var h=dojo.html;
_5ae=dojo.byId(_5ae);
_5af=dojo.byId(_5af);
var _5b4=h.toCoordinateObject(_5ae,true);
var _5b5=document.createElement("div");
h.copyStyle(_5b5,_5af);
if(_5af.explodeClassName){
_5b5.className=_5af.explodeClassName;
}
with(_5b5.style){
position="absolute";
display="none";
}
dojo.body().appendChild(_5b5);
with(_5af.style){
visibility="hidden";
display="block";
}
var _5b6=h.toCoordinateObject(_5af,true);
with(_5af.style){
display="none";
visibility="visible";
}
var _5b7={opacity:{start:0.5,end:1}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_5b7[type]={start:_5b4[type],end:_5b6[type]};
});
var anim=new dojo.lfx.propertyAnimation(_5b5,_5b7,_5b0,_5b1,{"beforeBegin":function(){
h.setDisplay(_5b5,"block");
},"onEnd":function(){
h.setDisplay(_5af,"block");
_5b5.parentNode.removeChild(_5b5);
}});
if(_5b2){
anim.connect("onEnd",function(){
_5b2(_5af,anim);
});
}
return anim;
};
dojo.lfx.html.implode=function(_5ba,end,_5bc,_5bd,_5be){
var h=dojo.html;
_5ba=dojo.byId(_5ba);
end=dojo.byId(end);
var _5c0=dojo.html.toCoordinateObject(_5ba,true);
var _5c1=dojo.html.toCoordinateObject(end,true);
var _5c2=document.createElement("div");
dojo.html.copyStyle(_5c2,_5ba);
if(_5ba.explodeClassName){
_5c2.className=_5ba.explodeClassName;
}
dojo.html.setOpacity(_5c2,0.3);
with(_5c2.style){
position="absolute";
display="none";
backgroundColor=h.getStyle(_5ba,"background-color").toLowerCase();
}
dojo.body().appendChild(_5c2);
var _5c3={opacity:{start:1,end:0.5}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_5c3[type]={start:_5c0[type],end:_5c1[type]};
});
var anim=new dojo.lfx.propertyAnimation(_5c2,_5c3,_5bc,_5bd,{"beforeBegin":function(){
dojo.html.hide(_5ba);
dojo.html.show(_5c2);
},"onEnd":function(){
_5c2.parentNode.removeChild(_5c2);
}});
if(_5be){
anim.connect("onEnd",function(){
_5be(_5ba,anim);
});
}
return anim;
};
dojo.lfx.html.highlight=function(_5c6,_5c7,_5c8,_5c9,_5ca){
_5c6=dojo.lfx.html._byId(_5c6);
var _5cb=[];
dojo.lang.forEach(_5c6,function(node){
var _5cd=dojo.html.getBackgroundColor(node);
var bg=dojo.html.getStyle(node,"background-color").toLowerCase();
var _5cf=dojo.html.getStyle(node,"background-image");
var _5d0=(bg=="transparent"||bg=="rgba(0, 0, 0, 0)");
while(_5cd.length>3){
_5cd.pop();
}
var rgb=new dojo.gfx.color.Color(_5c7);
var _5d2=new dojo.gfx.color.Color(_5cd);
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:rgb,end:_5d2}},_5c8,_5c9,{"beforeBegin":function(){
if(_5cf){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+rgb.toRgb().join(",")+")";
},"onEnd":function(){
if(_5cf){
node.style.backgroundImage=_5cf;
}
if(_5d0){
node.style.backgroundColor="transparent";
}
if(_5ca){
_5ca(node,anim);
}
}});
_5cb.push(anim);
});
return dojo.lfx.combine(_5cb);
};
dojo.lfx.html.unhighlight=function(_5d4,_5d5,_5d6,_5d7,_5d8){
_5d4=dojo.lfx.html._byId(_5d4);
var _5d9=[];
dojo.lang.forEach(_5d4,function(node){
var _5db=new dojo.gfx.color.Color(dojo.html.getBackgroundColor(node));
var rgb=new dojo.gfx.color.Color(_5d5);
var _5dd=dojo.html.getStyle(node,"background-image");
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:_5db,end:rgb}},_5d6,_5d7,{"beforeBegin":function(){
if(_5dd){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+_5db.toRgb().join(",")+")";
},"onEnd":function(){
if(_5d8){
_5d8(node,anim);
}
}});
_5d9.push(anim);
});
return dojo.lfx.combine(_5d9);
};
dojo.lang.mixin(dojo.lfx,dojo.lfx.html);
dojo.provide("dojo.lfx.*");
dojo.provide("dojo.dnd.HtmlDragAndDrop");
dojo.declare("dojo.dnd.HtmlDragSource",dojo.dnd.DragSource,{dragClass:"",onDragStart:function(){
var _5df=new dojo.dnd.HtmlDragObject(this.dragObject,this.type);
if(this.dragClass){
_5df.dragClass=this.dragClass;
}
if(this.constrainToContainer){
_5df.constrainTo(this.constrainingContainer||this.domNode.parentNode);
}
return _5df;
},setDragHandle:function(node){
node=dojo.byId(node);
dojo.dnd.dragManager.unregisterDragSource(this);
this.domNode=node;
dojo.dnd.dragManager.registerDragSource(this);
},setDragTarget:function(node){
this.dragObject=node;
},constrainTo:function(_5e2){
this.constrainToContainer=true;
if(_5e2){
this.constrainingContainer=_5e2;
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
dojo.dnd.DragSource.call(this);
this.type=(type)||(this.domNode.nodeName.toLowerCase());
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
if(node.tagName.toLowerCase()=="tr"){
var doc=this.domNode.ownerDocument;
var _5ea=doc.createElement("table");
var _5eb=doc.createElement("tbody");
_5ea.appendChild(_5eb);
_5eb.appendChild(node);
var _5ec=this.domNode.childNodes;
var _5ed=node.childNodes;
for(var i=0;i<_5ec.length;i++){
if((_5ed[i])&&(_5ed[i].style)){
_5ed[i].style.width=dojo.html.getContentBox(_5ec[i]).width+"px";
}
}
node=_5ea;
}
if((dojo.render.html.ie55||dojo.render.html.ie60)&&this.createIframe){
with(node.style){
top="0px";
left="0px";
}
var _5ef=document.createElement("div");
_5ef.appendChild(node);
this.bgIframe=new dojo.html.BackgroundIframe(_5ef);
_5ef.appendChild(this.bgIframe.iframe);
node=_5ef;
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
var _5f1=dojo.html.getViewport();
var _5f2=_5f1.width;
var _5f3=_5f1.height;
var _5f4=dojo.html.getScroll().offset;
var x=_5f4.x;
var y=_5f4.y;
}else{
var _5f7=dojo.html.getContentBox(this.constrainingContainer);
_5f2=_5f7.width;
_5f3=_5f7.height;
x=this.containingBlockPosition.x+dojo.html.getPixelValue(this.constrainingContainer,"padding-left",true)+dojo.html.getBorderExtent(this.constrainingContainer,"left");
y=this.containingBlockPosition.y+dojo.html.getPixelValue(this.constrainingContainer,"padding-top",true)+dojo.html.getBorderExtent(this.constrainingContainer,"top");
}
var mb=dojo.html.getMarginBox(this.domNode);
return {minX:x,minY:y,maxX:x+_5f2-mb.width,maxY:y+_5f3-mb.height};
},updateDragOffset:function(){
var _5f9=dojo.html.getScroll().offset;
if(_5f9.y!=this.scrollOffset.y){
var diff=_5f9.y-this.scrollOffset.y;
this.dragOffset.y+=diff;
this.scrollOffset.y=_5f9.y;
}
if(_5f9.x!=this.scrollOffset.x){
var diff=_5f9.x-this.scrollOffset.x;
this.dragOffset.x+=diff;
this.scrollOffset.x=_5f9.x;
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
var _601=dojo.html.getAbsolutePosition(this.dragClone,true);
var _602={left:this.dragStartPosition.x+1,top:this.dragStartPosition.y+1};
var anim=dojo.lfx.slideTo(this.dragClone,_602,500,dojo.lfx.easeOut);
var _604=this;
dojo.event.connect(anim,"onEnd",function(e){
dojo.lang.setTimeout(function(){
dojo.html.removeNode(_604.dragClone);
_604.dragClone=null;
},200);
});
anim.play();
break;
}
dojo.event.topic.publish("dragEnd",{source:this});
},constrainTo:function(_606){
this.constrainToContainer=true;
if(_606){
this.constrainingContainer=_606;
}else{
this.constrainingContainer=this.domNode.parentNode;
}
}},function(node,type){
this.domNode=dojo.byId(node);
this.type=type;
this.constrainToContainer=false;
this.dragSource=null;
});
dojo.declare("dojo.dnd.HtmlDropTarget",dojo.dnd.DropTarget,{vertical:false,onDragOver:function(e){
if(!this.accepts(e.dragObjects)){
return false;
}
this.childBoxes=[];
for(var i=0,_60b;i<this.domNode.childNodes.length;i++){
_60b=this.domNode.childNodes[i];
if(_60b.nodeType!=dojo.html.ELEMENT_NODE){
continue;
}
var pos=dojo.html.getAbsolutePosition(_60b,true);
var _60d=dojo.html.getBorderBox(_60b);
this.childBoxes.push({top:pos.y,bottom:pos.y+_60d.height,left:pos.x,right:pos.x+_60d.width,height:_60d.height,width:_60d.width,node:_60b});
}
return true;
},_getNodeUnderMouse:function(e){
for(var i=0,_610;i<this.childBoxes.length;i++){
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
},onDragMove:function(e,_612){
var i=this._getNodeUnderMouse(e);
if(!this.dropIndicator){
this.createDropIndicator();
}
var _614=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
var hide=false;
if(i<0){
if(this.childBoxes.length){
var _616=(dojo.html.gravity(this.childBoxes[0].node,e)&_614);
if(_616){
hide=true;
}
}else{
var _616=true;
}
}else{
var _617=this.childBoxes[i];
var _616=(dojo.html.gravity(_617.node,e)&_614);
if(_617.node===_612[0].dragSource.domNode){
hide=true;
}else{
var _618=_616?(i>0?this.childBoxes[i-1]:_617):(i<this.childBoxes.length-1?this.childBoxes[i+1]:_617);
if(_618.node===_612[0].dragSource.domNode){
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
this.placeIndicator(e,_612,i,_616);
if(!dojo.html.hasParent(this.dropIndicator)){
dojo.body().appendChild(this.dropIndicator);
}
},placeIndicator:function(e,_61a,_61b,_61c){
var _61d=this.vertical?"left":"top";
var _61e;
if(_61b<0){
if(this.childBoxes.length){
_61e=_61c?this.childBoxes[0]:this.childBoxes[this.childBoxes.length-1];
}else{
this.dropIndicator.style[_61d]=dojo.html.getAbsolutePosition(this.domNode,true)[this.vertical?"x":"y"]+"px";
}
}else{
_61e=this.childBoxes[_61b];
}
if(_61e){
this.dropIndicator.style[_61d]=(_61c?_61e[_61d]:_61e[this.vertical?"right":"bottom"])+"px";
if(this.vertical){
this.dropIndicator.style.height=_61e.height+"px";
this.dropIndicator.style.top=_61e.top+"px";
}else{
this.dropIndicator.style.width=_61e.width+"px";
this.dropIndicator.style.left=_61e.left+"px";
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
var _622=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
if(i<0){
if(this.childBoxes.length){
if(dojo.html.gravity(this.childBoxes[0].node,e)&_622){
return this.insert(e,this.childBoxes[0].node,"before");
}else{
return this.insert(e,this.childBoxes[this.childBoxes.length-1].node,"after");
}
}
return this.insert(e,this.domNode,"append");
}
var _623=this.childBoxes[i];
if(dojo.html.gravity(_623.node,e)&_622){
return this.insert(e,_623.node,"before");
}else{
return this.insert(e,_623.node,"after");
}
},insert:function(e,_625,_626){
var node=e.dragObject.domNode;
if(_626=="before"){
return dojo.html.insertBefore(node,_625);
}else{
if(_626=="after"){
return dojo.html.insertAfter(node,_625);
}else{
if(_626=="append"){
_625.appendChild(node);
return true;
}
}
}
return false;
}},function(node,_629){
if(arguments.length==0){
return;
}
this.domNode=dojo.byId(node);
dojo.dnd.DropTarget.call(this);
if(_629&&dojo.lang.isString(_629)){
_629=[_629];
}
this.acceptedTypes=_629||[];
});
dojo.provide("dojo.dnd.*");
dojo.provide("dojo.dnd.HtmlDragMove");
dojo.declare("dojo.dnd.HtmlDragMoveSource",dojo.dnd.HtmlDragSource,{onDragStart:function(){
var _62a=new dojo.dnd.HtmlDragMoveObject(this.dragObject,this.type);
if(this.constrainToContainer){
_62a.constrainTo(this.constrainingContainer);
}
return _62a;
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
dojo.string.repeat=function(str,_639,_63a){
var out="";
for(var i=0;i<_639;i++){
out+=str;
if(_63a&&i<_639-1){
out+=_63a;
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
dojo.io.Request=function(url,_649,_64a,_64b){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_649){
this.mimetype=_649;
}
if(_64a){
this.transport=_64a;
}
if(arguments.length>=4){
this.changeUrl=_64b;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_64e,_64f){
},error:function(type,_651,_652,_653){
},timeout:function(type,_655,_656,_657){
},handle:function(type,data,_65a,_65b){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_65c){
if(_65c["url"]){
_65c.url=_65c.url.toString();
}
if(_65c["formNode"]){
_65c.formNode=dojo.byId(_65c.formNode);
}
if(!_65c["method"]&&_65c["formNode"]&&_65c["formNode"].method){
_65c.method=_65c["formNode"].method;
}
if(!_65c["handle"]&&_65c["handler"]){
_65c.handle=_65c.handler;
}
if(!_65c["load"]&&_65c["loaded"]){
_65c.load=_65c.loaded;
}
if(!_65c["changeUrl"]&&_65c["changeURL"]){
_65c.changeUrl=_65c.changeURL;
}
_65c.encoding=dojo.lang.firstValued(_65c["encoding"],djConfig["bindEncoding"],"");
_65c.sendTransport=dojo.lang.firstValued(_65c["sendTransport"],djConfig["ioSendTransport"],false);
var _65d=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_65c[fn]&&_65d(_65c[fn])){
continue;
}
if(_65c["handle"]&&_65d(_65c["handle"])){
_65c[fn]=_65c.handle;
}
}
dojo.lang.mixin(this,_65c);
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
dojo.io.bind=function(_664){
if(!(_664 instanceof dojo.io.Request)){
try{
_664=new dojo.io.Request(_664);
}
catch(e){
dojo.debug(e);
}
}
var _665="";
if(_664["transport"]){
_665=_664["transport"];
if(!this[_665]){
dojo.io.sendBindError(_664,"No dojo.io.bind() transport with name '"+_664["transport"]+"'.");
return _664;
}
if(!this[_665].canHandle(_664)){
dojo.io.sendBindError(_664,"dojo.io.bind() transport with name '"+_664["transport"]+"' cannot handle this type of request.");
return _664;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_664))){
_665=tmp;
break;
}
}
if(_665==""){
dojo.io.sendBindError(_664,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _664;
}
}
this[_665].bind(_664);
_664.bindSuccess=true;
return _664;
};
dojo.io.sendBindError=function(_668,_669){
if((typeof _668.error=="function"||typeof _668.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _66a=new dojo.io.Error(_669);
setTimeout(function(){
_668[(typeof _668.error=="function")?"error":"handle"]("error",_66a,null,_668);
},50);
}else{
dojo.raise(_669);
}
};
dojo.io.queueBind=function(_66b){
if(!(_66b instanceof dojo.io.Request)){
try{
_66b=new dojo.io.Request(_66b);
}
catch(e){
dojo.debug(e);
}
}
var _66c=_66b.load;
_66b.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_66c.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _66e=_66b.error;
_66b.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_66e.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_66b);
dojo.io._dispatchNextQueueBind();
return _66b;
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
dojo.io.argsFromMap=function(map,_671,last){
var enc=/utf/i.test(_671||"")?encodeURIComponent:dojo.string.encodeAscii;
var _674=[];
var _675=new Object();
for(var name in map){
var _677=function(elt){
var val=enc(name)+"="+enc(elt);
_674[(last==name)?"push":"unshift"](val);
};
if(!_675[name]){
var _67a=map[name];
if(dojo.lang.isArray(_67a)){
dojo.lang.forEach(_67a,_677);
}else{
_677(_67a);
}
}
}
return _674.join("&");
};
dojo.io.setIFrameSrc=function(_67b,src,_67d){
try{
var r=dojo.render.html;
if(!_67d){
if(r.safari){
_67b.location=src;
}else{
frames[_67b.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_67b.contentWindow.document;
}else{
if(r.safari){
idoc=_67b.document;
}else{
idoc=_67b.contentWindow;
}
}
if(!idoc){
_67b.location=src;
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
dojo.AdapterRegistry=function(_680){
this.pairs=[];
this.returnWrappers=_680||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_682,wrap,_684,_685){
var type=(_685)?"unshift":"push";
this.pairs[type]([name,_682,wrap,_684]);
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
dojo.string.substituteParams=function(_68c,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _68c.replace(/\%\{(\w+)\}/g,function(_68f,key){
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
var _692=str.split(" ");
for(var i=0;i<_692.length;i++){
_692[i]=_692[i].charAt(0).toUpperCase()+_692[i].substring(1);
}
return _692.join(" ");
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
var _697=escape(str);
var _698,re=/%u([0-9A-F]{4})/i;
while((_698=_697.match(re))){
var num=Number("0x"+_698[1]);
var _69b=escape("&#"+num+";");
ret+=_697.substring(0,_698.index)+_69b;
_697=_697.substring(_698.index+_698[0].length);
}
ret+=_697.replace(/\+/g,"%2B");
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
dojo.string.escapeXml=function(str,_6a0){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_6a0){
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
dojo.string.endsWith=function(str,end,_6a9){
if(_6a9){
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
dojo.string.startsWith=function(str,_6ad,_6ae){
if(_6ae){
str=str.toLowerCase();
_6ad=_6ad.toLowerCase();
}
return str.indexOf(_6ad)==0;
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
dojo.string.normalizeNewlines=function(text,_6b4){
if(_6b4=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_6b4=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_6b6){
var _6b7=[];
for(var i=0,_6b9=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_6b6){
_6b7.push(str.substring(_6b9,i));
_6b9=i+1;
}
}
_6b7.push(str.substr(_6b9));
return _6b7;
};
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_6bb,wrap,_6bd){
dojo.json.jsonRegistry.register(name,_6bb,wrap,_6bd);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _6c0=typeof (o);
if(_6c0=="undefined"){
return "undefined";
}else{
if((_6c0=="number")||(_6c0=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_6c0=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _6c2;
if(typeof (o.__json__)=="function"){
_6c2=o.__json__();
if(o!==_6c2){
return me(_6c2);
}
}
if(typeof (o.json)=="function"){
_6c2=o.json();
if(o!==_6c2){
return me(_6c2);
}
}
if(_6c0!="function"&&typeof (o.length)=="number"){
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
_6c2=dojo.json.jsonRegistry.match(o);
return me(_6c2);
}
catch(e){
}
if(_6c0=="function"){
return null;
}
res=[];
for(var k in o){
var _6c7;
if(typeof (k)=="number"){
_6c7="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_6c7=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_6c7+":"+val);
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
var _6cc=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_6ce){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_6cc.apply(this,[_6ce]);
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
var _6cf=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_6d1){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_6cf){
_6cf.apply(this,[_6d1]);
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
},iframeLoaded:function(evt,_6d4){
if(!dojo.render.html.opera){
var _6d5=this._getUrlQuery(_6d4.href);
if(_6d5==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_6d5==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_6d5==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _6d6=this.historyStack.pop();
if(!_6d6){
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
this.forwardStack.push(_6d6);
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
var _6dd=url.split("?");
if(_6dd.length<2){
return null;
}else{
return _6dd[1];
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
var _6e0=false;
var _6e1=node.getElementsByTagName("input");
dojo.lang.forEach(_6e1,function(_6e2){
if(_6e0){
return;
}
if(_6e2.getAttribute("type")=="file"){
_6e0=true;
}
});
return _6e0;
};
dojo.io.formHasFile=function(_6e3){
return dojo.io.checkChildrenForFile(_6e3);
};
dojo.io.updateNode=function(node,_6e5){
node=dojo.byId(node);
var args=_6e5;
if(dojo.lang.isString(_6e5)){
args={url:_6e5};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
if(dojo["event"]){
try{
dojo.event.browser.clean(node.firstChild);
}
catch(e){
}
}
node.removeChild(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_6ec,_6ed,_6ee){
if((!_6ec)||(!_6ec.tagName)||(!_6ec.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_6ee){
_6ee=dojo.io.formFilter;
}
var enc=/utf/i.test(_6ed||"")?encodeURIComponent:dojo.string.encodeAscii;
var _6f0=[];
for(var i=0;i<_6ec.elements.length;i++){
var elm=_6ec.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_6ee(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_6f0.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_6f0.push(name+"="+enc(elm.value));
}
}else{
_6f0.push(name+"="+enc(elm.value));
}
}
}
var _6f6=_6ec.getElementsByTagName("input");
for(var i=0;i<_6f6.length;i++){
var _6f7=_6f6[i];
if(_6f7.type.toLowerCase()=="image"&&_6f7.form==_6ec&&_6ee(_6f7)){
var name=enc(_6f7.name);
_6f0.push(name+"="+enc(_6f7.value));
_6f0.push(name+".x=0");
_6f0.push(name+".y=0");
}
}
return _6f0.join("&")+"&";
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
var _6fd=form.getElementsByTagName("input");
for(var i=0;i<_6fd.length;i++){
var _6fe=_6fd[i];
if(_6fe.type.toLowerCase()=="image"&&_6fe.form==form){
this.connect(_6fe,"onclick","click");
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
var _705=false;
if(node.disabled||!node.name){
_705=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_705=node==this.clickedButton;
}else{
_705=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _705;
},connect:function(_706,_707,_708){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_706,_707,this,_708);
}else{
var fcn=dojo.lang.hitch(this,_708);
_706[_707]=function(e){
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
var _70b=this;
var _70c={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_70e,_70f){
return url+"|"+_70e+"|"+_70f.toLowerCase();
}
function addToCache(url,_711,_712,http){
_70c[getCacheKey(url,_711,_712)]=http;
}
function getFromCache(url,_715,_716){
return _70c[getCacheKey(url,_715,_716)];
}
this.clearCache=function(){
_70c={};
};
function doLoad(_717,http,url,_71a,_71b){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_717.method.toLowerCase()=="head"){
var _71d=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _71d;
};
var _71e=_71d.split(/[\r\n]+/g);
for(var i=0;i<_71e.length;i++){
var pair=_71e[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_717.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_717.mimetype=="text/json"||_717.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_717.mimetype=="application/xml")||(_717.mimetype=="text/xml")){
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
if(_71b){
addToCache(url,_71a,_717.method,http);
}
_717[(typeof _717.load=="function")?"load":"handle"]("load",ret,http,_717);
}else{
var _721=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_717[(typeof _717.error=="function")?"error":"handle"]("error",_721,http,_717);
}
}
function setHeaders(http,_723){
if(_723["headers"]){
for(var _724 in _723["headers"]){
if(_724.toLowerCase()=="content-type"&&!_723["contentType"]){
_723["contentType"]=_723["headers"][_724];
}else{
http.setRequestHeader(_724,_723["headers"][_724]);
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
if(!dojo.hostenv._blockAsync&&!_70b._blockAsync){
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
var _728=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_728,tif.http,tif.req);
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
var _729=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_72a){
return _729&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_72a["mimetype"].toLowerCase()||""))&&!(_72a["formNode"]&&dojo.io.formHasFile(_72a["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_72b){
if(!_72b["url"]){
if(!_72b["formNode"]&&(_72b["backButton"]||_72b["back"]||_72b["changeUrl"]||_72b["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_72b);
return true;
}
}
var url=_72b.url;
var _72d="";
if(_72b["formNode"]){
var ta=_72b.formNode.getAttribute("action");
if((ta)&&(!_72b["url"])){
url=ta;
}
var tp=_72b.formNode.getAttribute("method");
if((tp)&&(!_72b["method"])){
_72b.method=tp;
}
_72d+=dojo.io.encodeForm(_72b.formNode,_72b.encoding,_72b["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_72b["file"]){
_72b.method="post";
}
if(!_72b["method"]){
_72b.method="get";
}
if(_72b.method.toLowerCase()=="get"){
_72b.multipart=false;
}else{
if(_72b["file"]){
_72b.multipart=true;
}else{
if(!_72b["multipart"]){
_72b.multipart=false;
}
}
}
if(_72b["backButton"]||_72b["back"]||_72b["changeUrl"]){
dojo.undo.browser.addToHistory(_72b);
}
var _730=_72b["content"]||{};
if(_72b.sendTransport){
_730["dojo.transport"]="xmlhttp";
}
do{
if(_72b.postContent){
_72d=_72b.postContent;
break;
}
if(_730){
_72d+=dojo.io.argsFromMap(_730,_72b.encoding);
}
if(_72b.method.toLowerCase()=="get"||!_72b.multipart){
break;
}
var t=[];
if(_72d.length){
var q=_72d.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_72b.file){
if(dojo.lang.isArray(_72b.file)){
for(var i=0;i<_72b.file.length;++i){
var o=_72b.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_72b.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_72d=t.join("\r\n");
}
}while(false);
var _736=_72b["sync"]?false:true;
var _737=_72b["preventCache"]||(this.preventCache==true&&_72b["preventCache"]!=false);
var _738=_72b["useCache"]==true||(this.useCache==true&&_72b["useCache"]!=false);
if(!_737&&_738){
var _739=getFromCache(url,_72d,_72b.method);
if(_739){
doLoad(_72b,_739,url,_72d,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_72b);
var _73b=false;
if(_736){
var _73c=this.inFlight.push({"req":_72b,"http":http,"url":url,"query":_72d,"useCache":_738,"startTime":_72b.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_70b._blockAsync=true;
}
if(_72b.method.toLowerCase()=="post"){
if(!_72b.user){
http.open("POST",url,_736);
}else{
http.open("POST",url,_736,_72b.user,_72b.password);
}
setHeaders(http,_72b);
http.setRequestHeader("Content-Type",_72b.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_72b.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_72d);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_72b,{status:404},url,_72d,_738);
}
}else{
var _73d=url;
if(_72d!=""){
_73d+=(_73d.indexOf("?")>-1?"&":"?")+_72d;
}
if(_737){
_73d+=(dojo.string.endsWithAny(_73d,"?","&")?"":(_73d.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_72b.user){
http.open(_72b.method.toUpperCase(),_73d,_736);
}else{
http.open(_72b.method.toUpperCase(),_73d,_736,_72b.user,_72b.password);
}
setHeaders(http,_72b);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_72b,{status:404},url,_72d,_738);
}
}
if(!_736){
doLoad(_72b,http,url,_72d,_738);
_70b._blockAsync=false;
}
_72b.abort=function(){
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
dojo.io.createIFrame=function(_73e,_73f,uri){
if(window[_73e]){
return window[_73e];
}
if(window.frames[_73e]){
return window.frames[_73e];
}
var r=dojo.render.html;
var _742=null;
var turi=uri||dojo.uri.dojoUri("iframe_history.html?noInit=true");
var _744=((r.ie)&&(dojo.render.os.win))?"<iframe name=\""+_73e+"\" src=\""+turi+"\" onload=\""+_73f+"\">":"iframe";
_742=document.createElement(_744);
with(_742){
name=_73e;
setAttribute("name",_73e);
id=_73e;
}
dojo.body().appendChild(_742);
window[_73e]=_742;
with(_742.style){
if(!r.safari){
position="absolute";
}
left=top="0px";
height=width="1px";
visibility="hidden";
}
if(!r.ie){
dojo.io.setIFrameSrc(_742,turi,true);
_742.onload=new Function(_73f);
}
return _742;
};
dojo.io.IframeTransport=new function(){
var _745=this;
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
var _748=cr["content"]||{};
if(cr.sendTransport){
_748["dojo.transport"]="iframe";
}
if(fn){
if(_748){
for(var x in _748){
if(!fn[x]){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_748[x]+"'>");
fn.appendChild(tn);
}else{
tn=document.createElement("input");
fn.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_748[x];
}
cr._contentToClean.push(x);
}else{
fn[x].value=_748[x];
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
var _74b=dojo.io.argsFromMap(this.currentRequest.content);
var _74c=cr.url+(cr.url.indexOf("?")>-1?"&":"?")+_74b;
dojo.io.setIFrameSrc(this.iframe,_74c,true);
}
}
catch(e){
this.iframeOnload(e);
}
};
this.canHandle=function(_74d){
return ((dojo.lang.inArray(["text/plain","text/html","text/javascript","text/json","application/json"],_74d["mimetype"]))&&(dojo.lang.inArray(["post","get"],_74d["method"].toLowerCase()))&&(!((_74d["sync"])&&(_74d["sync"]==true))));
};
this.bind=function(_74e){
if(!this["iframe"]){
this.setUpIframe();
}
this.requestQueue.push(_74e);
this.fireNextRequest();
return;
};
this.setUpIframe=function(){
this.iframe=dojo.io.createIFrame(this.iframeName,"dojo.io.IframeTransport.iframeOnload();");
};
this.iframeOnload=function(_74f){
if(!_745.currentRequest){
_745.fireNextRequest();
return;
}
var req=_745.currentRequest;
if(req.formNode){
var _751=req._contentToClean;
for(var i=0;i<_751.length;i++){
var key=_751[i];
if(dojo.render.html.safari){
var _754=req.formNode;
for(var j=0;j<_754.childNodes.length;j++){
var _756=_754.childNodes[j];
if(_756.name==key){
var _757=_756.parentNode;
_757.removeChild(_756);
break;
}
}
}else{
var _758=req.formNode[key];
req.formNode.removeChild(_758);
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
var _759=function(_75a){
var doc=_75a.contentDocument||((_75a.contentWindow)&&(_75a.contentWindow.document))||((_75a.name)&&(document.frames[_75a.name])&&(document.frames[_75a.name].document))||null;
return doc;
};
var _75c;
var _75d=false;
if(_74f){
this._callError(req,"IframeTransport Request Error: "+_74f);
}else{
var ifd=_759(_745.iframe);
try{
var cmt=req.mimetype;
if((cmt=="text/javascript")||(cmt=="text/json")||(cmt=="application/json")){
var js=ifd.getElementsByTagName("textarea")[0].value;
if(cmt=="text/json"||cmt=="application/json"){
js="("+js+")";
}
_75c=dj_eval(js);
}else{
if(cmt=="text/html"){
_75c=ifd;
}else{
_75c=ifd.getElementsByTagName("textarea")[0].value;
}
}
_75d=true;
}
catch(e){
this._callError(req,"IframeTransport Error: "+e);
}
}
try{
if(_75d&&dojo.lang.isFunction(req["load"])){
req.load("load",_75c,req);
}
}
catch(e){
throw e;
}
finally{
_745.currentRequest=null;
_745.fireNextRequest();
}
};
this._callError=function(req,_762){
var _763=new dojo.io.Error(_762);
if(dojo.lang.isFunction(req["error"])){
req.error("error",_763,req);
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
var _764=0;
var _765=0;
for(var _766 in this._state){
_764++;
var _767=this._state[_766];
if(_767.isDone){
_765++;
delete this._state[_766];
}else{
if(!_767.isFinishing){
var _768=_767.kwArgs;
try{
if(_767.checkString&&eval("typeof("+_767.checkString+") != 'undefined'")){
_767.isFinishing=true;
this._finish(_767,"load");
_765++;
delete this._state[_766];
}else{
if(_768.timeoutSeconds&&_768.timeout){
if(_767.startTime+(_768.timeoutSeconds*1000)<(new Date()).getTime()){
_767.isFinishing=true;
this._finish(_767,"timeout");
_765++;
delete this._state[_766];
}
}else{
if(!_768.timeoutSeconds){
_765++;
}
}
}
}
catch(e){
_767.isFinishing=true;
this._finish(_767,"error",{status:this.DsrStatusCodes.Error,response:e});
}
}
}
}
if(_765>=_764){
clearInterval(this.inFlightTimer);
this.inFlightTimer=null;
}
};
this.canHandle=function(_769){
return dojo.lang.inArray(["text/javascript","text/json","application/json"],(_769["mimetype"].toLowerCase()))&&(_769["method"].toLowerCase()=="get")&&!(_769["formNode"]&&dojo.io.formHasFile(_769["formNode"]))&&(!_769["sync"]||_769["sync"]==false)&&!_769["file"]&&!_769["multipart"];
};
this.removeScripts=function(){
var _76a=document.getElementsByTagName("script");
for(var i=0;_76a&&i<_76a.length;i++){
var _76c=_76a[i];
if(_76c.className=="ScriptSrcTransport"){
var _76d=_76c.parentNode;
_76d.removeChild(_76c);
i--;
}
}
};
this.bind=function(_76e){
var url=_76e.url;
var _770="";
if(_76e["formNode"]){
var ta=_76e.formNode.getAttribute("action");
if((ta)&&(!_76e["url"])){
url=ta;
}
var tp=_76e.formNode.getAttribute("method");
if((tp)&&(!_76e["method"])){
_76e.method=tp;
}
_770+=dojo.io.encodeForm(_76e.formNode,_76e.encoding,_76e["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
var _773=url.split("?");
if(_773&&_773.length==2){
url=_773[0];
_770+=(_770?"&":"")+_773[1];
}
if(_76e["backButton"]||_76e["back"]||_76e["changeUrl"]){
dojo.undo.browser.addToHistory(_76e);
}
var id=_76e["apiId"]?_76e["apiId"]:"id"+this._counter++;
var _775=_76e["content"];
var _776=_76e.jsonParamName;
if(_76e.sendTransport||_776){
if(!_775){
_775={};
}
if(_76e.sendTransport){
_775["dojo.transport"]="scriptsrc";
}
if(_776){
_775[_776]="dojo.io.ScriptSrcTransport._state."+id+".jsonpCall";
}
}
if(_76e.postContent){
_770=_76e.postContent;
}else{
if(_775){
_770+=((_770)?"&":"")+dojo.io.argsFromMap(_775,_76e.encoding,_776);
}
}
if(_76e["apiId"]){
_76e["useRequestId"]=true;
}
var _777={"id":id,"idParam":"_dsrid="+id,"url":url,"query":_770,"kwArgs":_76e,"startTime":(new Date()).getTime(),"isFinishing":false};
if(!url){
this._finish(_777,"error",{status:this.DsrStatusCodes.Error,statusText:"url.none"});
return;
}
if(_775&&_775[_776]){
_777.jsonp=_775[_776];
_777.jsonpCall=function(data){
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
if(_76e["useRequestId"]||_76e["checkString"]||_777["jsonp"]){
this._state[id]=_777;
}
if(_76e["checkString"]){
_777.checkString=_76e["checkString"];
}
_777.constantParams=(_76e["constantParams"]==null?"":_76e["constantParams"]);
if(_76e["preventCache"]||(this.preventCache==true&&_76e["preventCache"]!=false)){
_777.nocacheParam="dojo.preventCache="+new Date().valueOf();
}else{
_777.nocacheParam="";
}
var _779=_777.url.length+_777.query.length+_777.constantParams.length+_777.nocacheParam.length+this._extraPaddingLength;
if(_76e["useRequestId"]){
_779+=_777.idParam.length;
}
if(!_76e["checkString"]&&_76e["useRequestId"]&&!_777["jsonp"]&&!_76e["forceSingleRequest"]&&_779>this.maxUrlLength){
if(url>this.maxUrlLength){
this._finish(_777,"error",{status:this.DsrStatusCodes.Error,statusText:"url.tooBig"});
return;
}else{
this._multiAttach(_777,1);
}
}else{
var _77a=[_777.constantParams,_777.nocacheParam,_777.query];
if(_76e["useRequestId"]&&!_777["jsonp"]){
_77a.unshift(_777.idParam);
}
var _77b=this._buildUrl(_777.url,_77a);
_777.finalUrl=_77b;
this._attach(_777.id,_77b);
}
this.startWatchingInFlight();
};
this._counter=1;
this._state={};
this._extraPaddingLength=16;
this._buildUrl=function(url,_77d){
var _77e=url;
var _77f="?";
for(var i=0;i<_77d.length;i++){
if(_77d[i]){
_77e+=_77f+_77d[i];
_77f="&";
}
}
return _77e;
};
this._attach=function(id,url){
var _783=document.createElement("script");
_783.type="text/javascript";
_783.src=url;
_783.id=id;
_783.className="ScriptSrcTransport";
document.getElementsByTagName("head")[0].appendChild(_783);
};
this._multiAttach=function(_784,part){
if(_784.query==null){
this._finish(_784,"error",{status:this.DsrStatusCodes.Error,statusText:"query.null"});
return;
}
if(!_784.constantParams){
_784.constantParams="";
}
var _786=this.maxUrlLength-_784.idParam.length-_784.constantParams.length-_784.url.length-_784.nocacheParam.length-this._extraPaddingLength;
var _787=_784.query.length<_786;
var _788;
if(_787){
_788=_784.query;
_784.query=null;
}else{
var _789=_784.query.lastIndexOf("&",_786-1);
var _78a=_784.query.lastIndexOf("=",_786-1);
if(_789>_78a||_78a==_786-1){
_788=_784.query.substring(0,_789);
_784.query=_784.query.substring(_789+1,_784.query.length);
}else{
_788=_784.query.substring(0,_786);
var _78b=_788.substring((_789==-1?0:_789+1),_78a);
_784.query=_78b+"="+_784.query.substring(_786,_784.query.length);
}
}
var _78c=[_788,_784.idParam,_784.constantParams,_784.nocacheParam];
if(!_787){
_78c.push("_part="+part);
}
var url=this._buildUrl(_784.url,_78c);
this._attach(_784.id+"_"+part,url);
};
this._finish=function(_78e,_78f,_790){
if(_78f!="partOk"&&!_78e.kwArgs[_78f]&&!_78e.kwArgs["handle"]){
if(_78f=="error"){
_78e.isDone=true;
throw _790;
}
}else{
switch(_78f){
case "load":
var _791=_790?_790.response:null;
if(!_791){
_791=_790;
}
_78e.kwArgs[(typeof _78e.kwArgs.load=="function")?"load":"handle"]("load",_791,_790,_78e.kwArgs);
_78e.isDone=true;
break;
case "partOk":
var part=parseInt(_790.response.part,10)+1;
if(_790.response.constantParams){
_78e.constantParams=_790.response.constantParams;
}
this._multiAttach(_78e,part);
_78e.isDone=false;
break;
case "error":
_78e.kwArgs[(typeof _78e.kwArgs.error=="function")?"error":"handle"]("error",_790.response,_790,_78e.kwArgs);
_78e.isDone=true;
break;
default:
_78e.kwArgs[(typeof _78e.kwArgs[_78f]=="function")?_78f:"handle"](_78f,_790,_790,_78e.kwArgs);
_78e.isDone=true;
}
}
};
dojo.io.transports.addTransport("ScriptSrcTransport");
};
window.onscriptload=function(_793){
var _794=null;
var _795=dojo.io.ScriptSrcTransport;
if(_795._state[_793.id]){
_794=_795._state[_793.id];
}else{
var _796;
for(var _797 in _795._state){
_796=_795._state[_797];
if(_796.finalUrl&&_796.finalUrl==_793.id){
_794=_796;
break;
}
}
if(_794==null){
var _798=document.getElementsByTagName("script");
for(var i=0;_798&&i<_798.length;i++){
var _79a=_798[i];
if(_79a.getAttribute("class")=="ScriptSrcTransport"&&_79a.src==_793.id){
_794=_795._state[_79a.id];
break;
}
}
}
if(_794==null){
throw "No matching state for onscriptload event.id: "+_793.id;
}
}
var _79b="error";
switch(_793.status){
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Continue:
_79b="partOk";
break;
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Ok:
_79b="load";
break;
}
_795._finish(_794,_79b,_793);
};
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_79d,days,path,_7a0,_7a1){
var _7a2=-1;
if(typeof days=="number"&&days>=0){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_7a2=d.toGMTString();
}
_79d=escape(_79d);
document.cookie=name+"="+_79d+";"+(_7a2!=-1?" expires="+_7a2+";":"")+(path?"path="+path:"")+(_7a0?"; domain="+_7a0:"")+(_7a1?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _7a6=document.cookie.substring(idx+name.length+1);
var end=_7a6.indexOf(";");
if(end==-1){
end=_7a6.length;
}
_7a6=_7a6.substring(0,end);
_7a6=unescape(_7a6);
return _7a6;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_7ad,_7ae,_7af){
if(arguments.length==5){
_7af=_7ad;
_7ad=null;
_7ae=null;
}
var _7b0=[],_7b1,_7b2="";
if(!_7af){
_7b1=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_7b1){
_7b1={};
}
for(var prop in obj){
if(prop==null){
delete _7b1[prop];
}else{
if(typeof obj[prop]=="string"||typeof obj[prop]=="number"){
_7b1[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _7b1){
_7b0.push(escape(prop)+"="+escape(_7b1[prop]));
}
_7b2=_7b0.join("&");
}
dojo.io.cookie.setCookie(name,_7b2,days,path,_7ad,_7ae);
};
dojo.io.cookie.getObjectCookie=function(name){
var _7b5=null,_7b6=dojo.io.cookie.getCookie(name);
if(_7b6){
_7b5={};
var _7b7=_7b6.split("&");
for(var i=0;i<_7b7.length;i++){
var pair=_7b7[i].split("=");
var _7ba=pair[1];
if(isNaN(_7ba)){
_7ba=unescape(pair[1]);
}
_7b5[unescape(pair[0])]=_7ba;
}
}
return _7b5;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _7bb=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_7bb=="CookiesAllowed");
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
this.tunnelInit=function(_7bc,_7bd){
};
this.tunnelCollapse=function(){
dojo.debug("tunnel collapsed!");
};
this.init=function(_7be,root,_7c0){
_7be=_7be||{};
_7be.version=this.version;
_7be.minimumVersion=this.minimumVersion;
_7be.channel="/meta/handshake";
this.url=root||djConfig["cometdRoot"];
if(!this.url){
dojo.debug("no cometd root specified in djConfig and no root passed");
return;
}
var _7c1={url:this.url,method:"POST",mimetype:"text/json",load:dojo.lang.hitch(this,"finishInit"),content:{"message":dojo.json.serialize([_7be])}};
var _7c2="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=(""+window.location).match(new RegExp(_7c2));
if(r[4]){
var tmp=r[4].split(":");
var _7c5=tmp[0];
var _7c6=tmp[1]||"80";
r=this.url.match(new RegExp(_7c2));
if(r[4]){
tmp=r[4].split(":");
var _7c7=tmp[0];
var _7c8=tmp[1]||"80";
if((_7c7!=_7c5)||(_7c8!=_7c6)){
dojo.debug(_7c5,_7c7);
dojo.debug(_7c6,_7c8);
this._isXD=true;
_7c1.transport="ScriptSrcTransport";
_7c1.jsonParamName="jsonp";
_7c1.method="GET";
}
}
}
if(_7c0){
dojo.lang.mixin(_7c1,_7c0);
}
return dojo.io.bind(_7c1);
};
this.finishInit=function(type,data,evt,_7cc){
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
this.deliver=function(_7cf){
dojo.lang.forEach(_7cf,this._deliver,this);
};
this._deliver=function(_7d0){
if(!_7d0["channel"]){
dojo.debug("cometd error: no channel for message!");
return;
}
if(!this.currentTransport){
this.backlog.push(["deliver",_7d0]);
return;
}
this.lastMessage=_7d0;
if((_7d0.channel.length>5)&&(_7d0.channel.substr(0,5)=="/meta")){
switch(_7d0.channel){
case "/meta/subscribe":
if(!_7d0.successful){
dojo.debug("cometd subscription error for channel",_7d0.channel,":",_7d0.error);
return;
}
this.subscribed(_7d0.subscription,_7d0);
break;
case "/meta/unsubscribe":
if(!_7d0.successful){
dojo.debug("cometd unsubscription error for channel",_7d0.channel,":",_7d0.error);
return;
}
this.unsubscribed(_7d0.subscription,_7d0);
break;
}
}
this.currentTransport.deliver(_7d0);
var _7d1=(this.globalTopicChannels[_7d0.channel])?_7d0.channel:"/cometd"+_7d0.channel;
dojo.event.topic.publish(_7d1,_7d0);
};
this.disconnect=function(){
if(!this.currentTransport){
dojo.debug("no current transport to disconnect from");
return;
}
this.currentTransport.disconnect();
};
this.publish=function(_7d2,data,_7d4){
if(!this.currentTransport){
this.backlog.push(["publish",_7d2,data,_7d4]);
return;
}
var _7d5={data:data,channel:_7d2};
if(_7d4){
dojo.lang.mixin(_7d5,_7d4);
}
return this.currentTransport.sendMessage(_7d5);
};
this.subscribe=function(_7d6,_7d7,_7d8,_7d9){
if(!this.currentTransport){
this.backlog.push(["subscribe",_7d6,_7d7,_7d8,_7d9]);
return;
}
if(_7d8){
var _7da=(_7d7)?_7d6:"/cometd"+_7d6;
if(_7d7){
this.globalTopicChannels[_7d6]=true;
}
dojo.event.topic.subscribe(_7da,_7d8,_7d9);
}
return this.currentTransport.sendMessage({channel:"/meta/subscribe",subscription:_7d6});
};
this.subscribed=function(_7db,_7dc){
dojo.debug(_7db);
dojo.debugShallow(_7dc);
};
this.unsubscribe=function(_7dd,_7de,_7df,_7e0){
if(!this.currentTransport){
this.backlog.push(["unsubscribe",_7dd,_7de,_7df,_7e0]);
return;
}
if(_7df){
var _7e1=(_7de)?_7dd:"/cometd"+_7dd;
dojo.event.topic.unsubscribe(_7e1,_7df,_7e0);
}
return this.currentTransport.sendMessage({channel:"/meta/unsubscribe",subscription:_7dd});
};
this.unsubscribed=function(_7e2,_7e3){
dojo.debug(_7e2);
dojo.debugShallow(_7e3);
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
this.check=function(_7e4,_7e5,_7e6){
return ((!_7e6)&&(!dojo.render.html.safari)&&(dojo.lang.inArray(_7e4,"iframe")));
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
this.deliver=function(_7e7){
if(_7e7["timestamp"]){
this.lastTimestamp=_7e7.timestamp;
}
if(_7e7["id"]){
this.lastId=_7e7.id;
}
if((_7e7.channel.length>5)&&(_7e7.channel.substr(0,5)=="/meta")){
switch(_7e7.channel){
case "/meta/connect":
if(!_7e7.successful){
dojo.debug("cometd connection error:",_7e7.error);
return;
}
this.connectionId=_7e7.connectionId;
this.connected=true;
this.processBacklog();
break;
case "/meta/reconnect":
if(!_7e7.successful){
dojo.debug("cometd reconnection error:",_7e7.error);
return;
}
this.connected=true;
break;
case "/meta/subscribe":
if(!_7e7.successful){
dojo.debug("cometd subscription error for channel",_7e7.channel,":",_7e7.error);
return;
}
dojo.debug(_7e7.channel);
break;
}
}
};
this.widenDomain=function(_7e8){
var cd=_7e8||document.domain;
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
this.postToIframe=function(_7eb,url){
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
for(var x in _7eb){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_7eb[x]+"'>");
this.phonyForm.appendChild(tn);
}else{
tn=document.createElement("input");
this.phonyForm.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_7eb[x];
}
}
this.phonyForm.submit();
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_7ef,_7f0){
if((_7f0)||(this.connected)){
_7ef.connectionId=this.connectionId;
_7ef.clientId=cometd.clientId;
var _7f1={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_7ef])}};
return dojo.io.bind(_7f1);
}else{
this.backlog.push(_7ef);
}
};
this.startup=function(_7f2){
dojo.debug("startup!");
dojo.debug(dojo.json.serialize(_7f2));
if(this.connected){
return;
}
this.rcvNodeName="cometdRcv_"+cometd._getRandStr();
var _7f3=cometd.url+"/?tunnelInit=iframe";
if(false&&dojo.render.html.ie){
this.rcvNode=new ActiveXObject("htmlfile");
this.rcvNode.open();
this.rcvNode.write("<html>");
this.rcvNode.write("<script>document.domain = '"+document.domain+"'");
this.rcvNode.write("</html>");
this.rcvNode.close();
var _7f4=this.rcvNode.createElement("div");
this.rcvNode.appendChild(_7f4);
this.rcvNode.parentWindow.dojo=dojo;
_7f4.innerHTML="<iframe src='"+_7f3+"'></iframe>";
}else{
this.rcvNode=dojo.io.createIFrame(this.rcvNodeName,"",_7f3);
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
this.check=function(_7f5,_7f6,_7f7){
return ((!_7f7)&&(dojo.render.html.mozilla)&&(dojo.lang.inArray(_7f5,"mime-message-block")));
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
this.openTunnelWith=function(_7f9,url){
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
dojo.debug(dojo.json.serialize(_7f9));
this.xhr.send(dojo.io.argsFromMap(_7f9,"utf8"));
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_7fb,_7fc){
if((_7fc)||(this.connected)){
_7fb.connectionId=this.connectionId;
_7fb.clientId=cometd.clientId;
var _7fd={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_7fb])}};
return dojo.io.bind(_7fd);
}else{
this.backlog.push(_7fb);
}
};
this.startup=function(_7fe){
dojo.debugShallow(_7fe);
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
this.check=function(_7ff,_800,_801){
return ((!_801)&&(dojo.lang.inArray(_7ff,"long-polling")));
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
this.openTunnelWith=function(_802,url){
dojo.io.bind({url:(url||cometd.url),method:"post",content:_802,mimetype:"text/json",load:dojo.lang.hitch(this,function(type,data,evt,args){
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
this.sendMessage=function(_808,_809){
if((_809)||(this.connected)){
_808.connectionId=this.connectionId;
_808.clientId=cometd.clientId;
var _80a={url:cometd.url||djConfig["cometdRoot"],method:"post",mimetype:"text/json",content:{message:dojo.json.serialize([_808])}};
return dojo.io.bind(_80a);
}else{
this.backlog.push(_808);
}
};
this.startup=function(_80b){
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
this.check=function(_80c,_80d,_80e){
return dojo.lang.inArray(_80c,"callback-polling");
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
this.openTunnelWith=function(_80f,url){
var req=dojo.io.bind({url:(url||cometd.url),content:_80f,mimetype:"text/json",transport:"ScriptSrcTransport",jsonParamName:"jsonp",load:dojo.lang.hitch(this,function(type,data,evt,args){
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
this.sendMessage=function(_816,_817){
if((_817)||(this.connected)){
_816.connectionId=this.connectionId;
_816.clientId=cometd.clientId;
var _818={url:cometd.url||djConfig["cometdRoot"],mimetype:"text/json",transport:"ScriptSrcTransport",jsonParamName:"jsonp",content:{message:dojo.json.serialize([_816])}};
return dojo.io.bind(_818);
}else{
this.backlog.push(_816);
}
};
this.startup=function(_819){
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
function getTagName(node){
return ((node)&&(node.tagName)?node.tagName.toLowerCase():"");
}
function getDojoTagName(node){
var _81c=getTagName(node);
if(!_81c){
return "";
}
if((dojo.widget)&&(dojo.widget.tags[_81c])){
return _81c;
}
var p=_81c.indexOf(":");
if(p>=0){
return _81c;
}
if(_81c.substr(0,5)=="dojo:"){
return _81c;
}
if(dojo.render.html.capable&&dojo.render.html.ie&&node.scopeName!="HTML"){
return node.scopeName.toLowerCase()+":"+_81c;
}
if(_81c.substr(0,4)=="dojo"){
return "dojo:"+_81c.substring(4);
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
if((!dj_global["djConfig"])||(djConfig["ignoreClassNames"])){
var _81f=node.className||node.getAttribute("class");
if((_81f)&&(_81f.indexOf)&&(_81f.indexOf("dojo-")!=-1)){
var _820=_81f.split(" ");
for(var x=0,c=_820.length;x<c;x++){
if(_820[x].slice(0,5)=="dojo-"){
return "dojo:"+_820[x].substr(5).toLowerCase();
}
}
}
}
return "";
}
this.parseElement=function(node,_824,_825,_826){
var _827={};
var _828=getTagName(node);
if((_828)&&(_828.indexOf("/")==0)){
return null;
}
var _829=true;
if(_825){
var _82a=getDojoTagName(node);
_828=_82a||_828;
_829=Boolean(_82a);
}
if(node&&node.getAttribute&&node.getAttribute("parseWidgets")&&node.getAttribute("parseWidgets")=="false"){
return {};
}
_827[_828]=[];
var pos=_828.indexOf(":");
if(pos>0){
var ns=_828.substring(0,pos);
_827["ns"]=ns;
if((dojo.ns)&&(!dojo.ns.allow(ns))){
_829=false;
}
}
if(_829){
var _82d=this.parseAttributes(node);
for(var attr in _82d){
if((!_827[_828][attr])||(typeof _827[_828][attr]!="array")){
_827[_828][attr]=[];
}
_827[_828][attr].push(_82d[attr]);
}
_827[_828].nodeRef=node;
_827.tagName=_828;
_827.index=_826||0;
}
var _82f=0;
for(var i=0;i<node.childNodes.length;i++){
var tcn=node.childNodes.item(i);
switch(tcn.nodeType){
case dojo.dom.ELEMENT_NODE:
_82f++;
var ctn=getDojoTagName(tcn)||getTagName(tcn);
if(!_827[ctn]){
_827[ctn]=[];
}
_827[ctn].push(this.parseElement(tcn,true,_825,_82f));
if((tcn.childNodes.length==1)&&(tcn.childNodes.item(0).nodeType==dojo.dom.TEXT_NODE)){
_827[ctn][_827[ctn].length-1].value=tcn.childNodes.item(0).nodeValue;
}
break;
case dojo.dom.TEXT_NODE:
if(node.childNodes.length==1){
_827[_828].push({value:node.childNodes.item(0).nodeValue});
}
break;
default:
break;
}
}
return _827;
};
this.parseAttributes=function(node){
var _834={};
var atts=node.attributes;
var _836,i=0;
while((_836=atts[i++])){
if((dojo.render.html.capable)&&(dojo.render.html.ie)){
if(!_836){
continue;
}
if((typeof _836=="object")&&(typeof _836.nodeValue=="undefined")||(_836.nodeValue==null)||(_836.nodeValue=="")){
continue;
}
}
var nn=_836.nodeName.split(":");
nn=(nn.length==2)?nn[1]:_836.nodeName;
_834[nn]={value:_836.nodeValue};
}
return _834;
};
};
dojo.provide("dojo.widget.Manager");
dojo.widget.manager=new function(){
this.widgets=[];
this.widgetIds=[];
this.topWidgets={};
var _839={};
var _83a=[];
this.getUniqueId=function(_83b){
var _83c;
do{
_83c=_83b+"_"+(_839[_83b]!=undefined?++_839[_83b]:_839[_83b]=0);
}while(this.getWidgetById(_83c));
return _83c;
};
this.add=function(_83d){
this.widgets.push(_83d);
if(!_83d.extraArgs["id"]){
_83d.extraArgs["id"]=_83d.extraArgs["ID"];
}
if(_83d.widgetId==""){
if(_83d["id"]){
_83d.widgetId=_83d["id"];
}else{
if(_83d.extraArgs["id"]){
_83d.widgetId=_83d.extraArgs["id"];
}else{
_83d.widgetId=this.getUniqueId(_83d.ns+"_"+_83d.widgetType);
}
}
}
if(this.widgetIds[_83d.widgetId]){
dojo.debug("widget ID collision on ID: "+_83d.widgetId);
}
this.widgetIds[_83d.widgetId]=_83d;
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
this.remove=function(_83f){
if(dojo.lang.isNumber(_83f)){
var tw=this.widgets[_83f].widgetId;
delete this.widgetIds[tw];
this.widgets.splice(_83f,1);
}else{
this.removeById(_83f);
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
var _846=(type.indexOf(":")<0?function(x){
return x.widgetType.toLowerCase();
}:function(x){
return x.getNamespacedType();
});
var ret=[];
dojo.lang.forEach(this.widgets,function(x){
if(_846(x)==lt){
ret.push(x);
}
});
return ret;
};
this.getWidgetsByFilter=function(_84b,_84c){
var ret=[];
dojo.lang.every(this.widgets,function(x){
if(_84b(x)){
ret.push(x);
if(_84c){
return false;
}
}
return true;
});
return (_84c?ret[0]:ret);
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
var _852={};
var _853=["dojo.widget"];
for(var i=0;i<_853.length;i++){
_853[_853[i]]=true;
}
this.registerWidgetPackage=function(_855){
if(!_853[_855]){
_853[_855]=true;
_853.push(_855);
}
};
this.getWidgetPackageList=function(){
return dojo.lang.map(_853,function(elt){
return (elt!==true?elt:undefined);
});
};
this.getImplementation=function(_857,_858,_859,ns){
var impl=this.getImplementationName(_857,ns);
if(impl){
var ret=_858?new impl(_858):new impl();
return ret;
}
};
function buildPrefixCache(){
for(var _85d in dojo.render){
if(dojo.render[_85d]["capable"]===true){
var _85e=dojo.render[_85d].prefixes;
for(var i=0;i<_85e.length;i++){
_83a.push(_85e[i].toLowerCase());
}
}
}
}
var _860=function(_861,_862){
if(!_862){
return null;
}
for(var i=0,l=_83a.length,_865;i<=l;i++){
_865=(i<l?_862[_83a[i]]:_862);
if(!_865){
continue;
}
for(var name in _865){
if(name.toLowerCase()==_861){
return _865[name];
}
}
}
return null;
};
var _867=function(_868,_869){
var _86a=dojo.evalObjPath(_869,false);
return (_86a?_860(_868,_86a):null);
};
this.getImplementationName=function(_86b,ns){
var _86d=_86b.toLowerCase();
ns=ns||"dojo";
var imps=_852[ns]||(_852[ns]={});
var impl=imps[_86d];
if(impl){
return impl;
}
if(!_83a.length){
buildPrefixCache();
}
var _870=dojo.ns.get(ns);
if(!_870){
dojo.ns.register(ns,ns+".widget");
_870=dojo.ns.get(ns);
}
if(_870){
_870.resolve(_86b);
}
impl=_867(_86d,_870.module);
if(impl){
return (imps[_86d]=impl);
}
_870=dojo.ns.require(ns);
if((_870)&&(_870.resolver)){
_870.resolve(_86b);
impl=_867(_86d,_870.module);
if(impl){
return (imps[_86d]=impl);
}
}
dojo.deprecated("dojo.widget.Manager.getImplementationName","Could not locate widget implementation for \""+_86b+"\" in \""+_870.module+"\" registered to namespace \""+_870.name+"\". "+"Developers must specify correct namespaces for all non-Dojo widgets","0.5");
for(var i=0;i<_853.length;i++){
impl=_867(_86d,_853[i]);
if(impl){
return (imps[_86d]=impl);
}
}
throw new Error("Could not locate widget implementation for \""+_86b+"\" in \""+_870.module+"\" registered to namespace \""+_870.name+"\"");
};
this.resizing=false;
this.onWindowResized=function(){
if(this.resizing){
return;
}
try{
this.resizing=true;
for(var id in this.topWidgets){
var _873=this.topWidgets[id];
if(_873.checkSize){
_873.checkSize();
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
var g=function(_878,_879){
dw[(_879||_878)]=h(_878);
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
var _87b=dwm.getAllWidgets.apply(dwm,arguments);
if(arguments.length>0){
return _87b[n];
}
return _87b;
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
var _87d=null;
if(window.getComputedStyle){
var _87e=getComputedStyle(div,"");
_87d=_87e.getPropertyValue("background-image");
}else{
_87d=div.currentStyle.backgroundImage;
}
var _87f=false;
if(_87d!=null&&(_87d=="none"||_87d=="url(invalid-url:)")){
this.accessible=true;
}
dojo.body().removeChild(div);
}
return this.accessible;
},setCheckAccessible:function(_880){
this.doAccessibleCheck=_880;
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
var _882=this.children[i];
if(_882.onResized){
_882.onResized();
}
}
},create:function(args,_884,_885,ns){
if(ns){
this.ns=ns;
}
this.satisfyPropertySets(args,_884,_885);
this.mixInProperties(args,_884,_885);
this.postMixInProperties(args,_884,_885);
dojo.widget.manager.add(this);
this.buildRendering(args,_884,_885);
this.initialize(args,_884,_885);
this.postInitialize(args,_884,_885);
this.postCreate(args,_884,_885);
return this;
},destroy:function(_887){
this.destroyChildren();
this.uninitialize();
this.destroyRendering(_887);
dojo.widget.manager.removeById(this.widgetId);
},destroyChildren:function(){
var _888;
var i=0;
while(this.children.length>i){
_888=this.children[i];
if(_888 instanceof dojo.widget.Widget){
this.removeChild(_888);
_888.destroy();
continue;
}
i++;
}
},getChildrenOfType:function(type,_88b){
var ret=[];
var _88d=dojo.lang.isFunction(type);
if(!_88d){
type=type.toLowerCase();
}
for(var x=0;x<this.children.length;x++){
if(_88d){
if(this.children[x] instanceof type){
ret.push(this.children[x]);
}
}else{
if(this.children[x].widgetType.toLowerCase()==type){
ret.push(this.children[x]);
}
}
if(_88b){
ret=ret.concat(this.children[x].getChildrenOfType(type,_88b));
}
}
return ret;
},getDescendants:function(){
var _88f=[];
var _890=[this];
var elem;
while((elem=_890.pop())){
_88f.push(elem);
if(elem.children){
dojo.lang.forEach(elem.children,function(elem){
_890.push(elem);
});
}
}
return _88f;
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
var _897;
var _898=dojo.widget.lcArgsCache[this.widgetType];
if(_898==null){
_898={};
for(var y in this){
_898[((new String(y)).toLowerCase())]=y;
}
dojo.widget.lcArgsCache[this.widgetType]=_898;
}
var _89a={};
for(var x in args){
if(!this[x]){
var y=_898[(new String(x)).toLowerCase()];
if(y){
args[y]=args[x];
x=y;
}
}
if(_89a[x]){
continue;
}
_89a[x]=true;
if((typeof this[x])!=(typeof _897)){
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
this[x]=args[x];
}else{
var _89c=args[x].split(";");
for(var y=0;y<_89c.length;y++){
var si=_89c[y].indexOf(":");
if((si!=-1)&&(_89c[y].length>si)){
this[x][_89c[y].substr(0,si).replace(/^\s+|\s+$/g,"")]=_89c[y].substr(si+1);
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
},postMixInProperties:function(args,frag,_8a0){
},initialize:function(args,frag,_8a3){
return false;
},postInitialize:function(args,frag,_8a6){
return false;
},postCreate:function(args,frag,_8a9){
return false;
},uninitialize:function(){
return false;
},buildRendering:function(args,frag,_8ac){
dojo.unimplemented("dojo.widget.Widget.buildRendering, on "+this.toString()+", ");
return false;
},destroyRendering:function(){
dojo.unimplemented("dojo.widget.Widget.destroyRendering");
return false;
},addedTo:function(_8ad){
},addChild:function(_8ae){
dojo.unimplemented("dojo.widget.Widget.addChild");
return false;
},removeChild:function(_8af){
for(var x=0;x<this.children.length;x++){
if(this.children[x]===_8af){
this.children.splice(x,1);
break;
}
}
return _8af;
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
dojo.widget.tags["dojo:propertyset"]=function(_8b4,_8b5,_8b6){
var _8b7=_8b5.parseProperties(_8b4["dojo:propertyset"]);
};
dojo.widget.tags["dojo:connect"]=function(_8b8,_8b9,_8ba){
var _8bb=_8b9.parseProperties(_8b8["dojo:connect"]);
};
dojo.widget.buildWidgetFromParseTree=function(type,frag,_8be,_8bf,_8c0,_8c1){
dojo.a11y.setAccessibleMode();
var _8c2=type.split(":");
_8c2=(_8c2.length==2)?_8c2[1]:type;
var _8c3=_8c1||_8be.parseProperties(frag[frag["ns"]+":"+_8c2]);
var _8c4=dojo.widget.manager.getImplementation(_8c2,null,null,frag["ns"]);
if(!_8c4){
throw new Error("cannot find \""+type+"\" widget");
}else{
if(!_8c4.create){
throw new Error("\""+type+"\" widget object has no \"create\" method and does not appear to implement *Widget");
}
}
_8c3["dojoinsertionindex"]=_8c0;
var ret=_8c4.create(_8c3,frag,_8bf,frag["ns"]);
return ret;
};
dojo.widget.defineWidget=function(_8c6,_8c7,_8c8,init,_8ca){
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
dojo.widget._defineWidget=function(_8cd,_8ce,_8cf,init,_8d1){
var _8d2=_8cd.split(".");
var type=_8d2.pop();
var regx="\\.("+(_8ce?_8ce+"|":"")+dojo.widget.defineWidget.renderers+")\\.";
var r=_8cd.search(new RegExp(regx));
_8d2=(r<0?_8d2.join("."):_8cd.substr(0,r));
dojo.widget.manager.registerWidgetPackage(_8d2);
var pos=_8d2.indexOf(".");
var _8d7=(pos>-1)?_8d2.substring(0,pos):_8d2;
_8d1=(_8d1)||{};
_8d1.widgetType=type;
if((!init)&&(_8d1["classConstructor"])){
init=_8d1.classConstructor;
delete _8d1.classConstructor;
}
dojo.declare(_8cd,_8cf,init,_8d1);
};
dojo.provide("dojo.widget.Parse");
dojo.widget.Parse=function(_8d8){
this.propertySetsList=[];
this.fragment=_8d8;
this.createComponents=function(frag,_8da){
var _8db=[];
var _8dc=false;
try{
if(frag&&frag.tagName&&(frag!=frag.nodeRef)){
var _8dd=dojo.widget.tags;
var tna=String(frag.tagName).split(";");
for(var x=0;x<tna.length;x++){
var ltn=tna[x].replace(/^\s+|\s+$/g,"").toLowerCase();
frag.tagName=ltn;
var ret;
if(_8dd[ltn]){
_8dc=true;
ret=_8dd[ltn](frag,this,_8da,frag.index);
_8db.push(ret);
}else{
if(ltn.indexOf(":")==-1){
ltn="dojo:"+ltn;
}
ret=dojo.widget.buildWidgetFromParseTree(ltn,frag,this,_8da,frag.index);
if(ret){
_8dc=true;
_8db.push(ret);
}
}
}
}
}
catch(e){
dojo.debug("dojo.widget.Parse: error:",e);
}
if(!_8dc){
_8db=_8db.concat(this.createSubComponents(frag,_8da));
}
return _8db;
};
this.createSubComponents=function(_8e2,_8e3){
var frag,_8e5=[];
for(var item in _8e2){
frag=_8e2[item];
if(frag&&typeof frag=="object"&&(frag!=_8e2.nodeRef)&&(frag!=_8e2.tagName)&&(!dojo.dom.isNode(frag))){
_8e5=_8e5.concat(this.createComponents(frag,_8e3));
}
}
return _8e5;
};
this.parsePropertySets=function(_8e7){
return [];
};
this.parseProperties=function(_8e8){
var _8e9={};
for(var item in _8e8){
if((_8e8[item]==_8e8.tagName)||(_8e8[item]==_8e8.nodeRef)){
}else{
var frag=_8e8[item];
if(frag.tagName&&dojo.widget.tags[frag.tagName.toLowerCase()]){
}else{
if(frag[0]&&frag[0].value!=""&&frag[0].value!=null){
try{
if(item.toLowerCase()=="dataprovider"){
var _8ec=this;
this.getDataProvider(_8ec,frag[0].value);
_8e9.dataProvider=this.dataProvider;
}
_8e9[item]=frag[0].value;
var _8ed=this.parseProperties(frag);
for(var _8ee in _8ed){
_8e9[_8ee]=_8ed[_8ee];
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
if(typeof _8e9[item]!="boolean"){
_8e9[item]=true;
}
break;
}
}
}
return _8e9;
};
this.getDataProvider=function(_8ef,_8f0){
dojo.io.bind({url:_8f0,load:function(type,_8f2){
if(type=="load"){
_8ef.dataProvider=_8f2;
}
},mimetype:"text/javascript",sync:true});
};
this.getPropertySetById=function(_8f3){
for(var x=0;x<this.propertySetsList.length;x++){
if(_8f3==this.propertySetsList[x]["id"][0].value){
return this.propertySetsList[x];
}
}
return "";
};
this.getPropertySetsByType=function(_8f5){
var _8f6=[];
for(var x=0;x<this.propertySetsList.length;x++){
var cpl=this.propertySetsList[x];
var cpcc=cpl.componentClass||cpl.componentType||null;
var _8fa=this.propertySetsList[x]["id"][0].value;
if(cpcc&&(_8fa==cpcc[0].value)){
_8f6.push(cpl);
}
}
return _8f6;
};
this.getPropertySets=function(_8fb){
var ppl="dojo:propertyproviderlist";
var _8fd=[];
var _8fe=_8fb.tagName;
if(_8fb[ppl]){
var _8ff=_8fb[ppl].value.split(" ");
for(var _900 in _8ff){
if((_900.indexOf("..")==-1)&&(_900.indexOf("://")==-1)){
var _901=this.getPropertySetById(_900);
if(_901!=""){
_8fd.push(_901);
}
}else{
}
}
}
return this.getPropertySetsByType(_8fe).concat(_8fd);
};
this.createComponentFromScript=function(_902,_903,_904,ns){
_904.fastMixIn=true;
var ltn=(ns||"dojo")+":"+_903.toLowerCase();
if(dojo.widget.tags[ltn]){
return [dojo.widget.tags[ltn](_904,this,null,null,_904)];
}
return [dojo.widget.buildWidgetFromParseTree(ltn,_904,this,null,null,_904)];
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
dojo.widget.createWidget=function(name,_909,_90a,_90b){
var _90c=false;
var _90d=(typeof name=="string");
if(_90d){
var pos=name.indexOf(":");
var ns=(pos>-1)?name.substring(0,pos):"dojo";
if(pos>-1){
name=name.substring(pos+1);
}
var _910=name.toLowerCase();
var _911=ns+":"+_910;
_90c=(dojo.byId(name)&&!dojo.widget.tags[_911]);
}
if((arguments.length==1)&&(_90c||!_90d)){
var xp=new dojo.xml.Parse();
var tn=_90c?dojo.byId(name):name;
return dojo.widget.getParser().createComponents(xp.parseElement(tn,null,true))[0];
}
function fromScript(_914,name,_916,ns){
_916[_911]={dojotype:[{value:_910}],nodeRef:_914,fastMixIn:true};
_916.ns=ns;
return dojo.widget.getParser().createComponentFromScript(_914,name,_916,ns);
}
_909=_909||{};
var _918=false;
var tn=null;
var h=dojo.render.html.capable;
if(h){
tn=document.createElement("span");
}
if(!_90a){
_918=true;
_90a=tn;
if(h){
dojo.body().appendChild(_90a);
}
}else{
if(_90b){
dojo.dom.insertAtPosition(tn,_90a,_90b);
}else{
tn=_90a;
}
}
var _91a=fromScript(tn,name.toLowerCase(),_909,ns);
if((!_91a)||(!_91a[0])||(typeof _91a[0].widgetType=="undefined")){
throw new Error("createWidget: Creation of \""+name+"\" widget failed.");
}
try{
if(_918&&_91a[0].domNode.parentNode){
_91a[0].domNode.parentNode.removeChild(_91a[0].domNode);
}
}
catch(e){
dojo.debug(e);
}
return _91a[0];
};
dojo.provide("dojo.io.*");
dojo.provide("dojo.widget.DomWidget");
dojo.widget._cssFiles={};
dojo.widget._cssStrings={};
dojo.widget._templateCache={};
dojo.widget.defaultStrings={dojoRoot:dojo.hostenv.getBaseScriptUri(),baseScriptUri:dojo.hostenv.getBaseScriptUri()};
dojo.widget.fillFromTemplateCache=function(obj,_91c,_91d,_91e){
var _91f=_91c||obj.templatePath;
var _920=dojo.widget._templateCache;
if(!_91f&&!obj["widgetType"]){
do{
var _921="__dummyTemplate__"+dojo.widget._templateCache.dummyCount++;
}while(_920[_921]);
obj.widgetType=_921;
}
var wt=_91f?_91f.toString():obj.widgetType;
var ts=_920[wt];
if(!ts){
_920[wt]={"string":null,"node":null};
if(_91e){
ts={};
}else{
ts=_920[wt];
}
}
if((!obj.templateString)&&(!_91e)){
obj.templateString=_91d||ts["string"];
}
if((!obj.templateNode)&&(!_91e)){
obj.templateNode=ts["node"];
}
if((!obj.templateNode)&&(!obj.templateString)&&(_91f)){
var _924=dojo.hostenv.getText(_91f);
if(_924){
_924=_924.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");
var _925=_924.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_925){
_924=_925[1];
}
}else{
_924="";
}
obj.templateString=_924;
if(!_91e){
_920[wt]["string"]=_924;
}
}
if((!ts["string"])&&(!_91e)){
ts.string=obj.templateString;
}
};
dojo.widget._templateCache.dummyCount=0;
dojo.widget.attachProperties=["dojoAttachPoint","id"];
dojo.widget.eventAttachProperty="dojoAttachEvent";
dojo.widget.onBuildProperty="dojoOnBuild";
dojo.widget.waiNames=["waiRole","waiState"];
dojo.widget.wai={waiRole:{name:"waiRole","namespace":"http://www.w3.org/TR/xhtml2",alias:"x2",prefix:"wairole:"},waiState:{name:"waiState","namespace":"http://www.w3.org/2005/07/aaa",alias:"aaa",prefix:""},setAttr:function(node,ns,attr,_929){
if(dojo.render.html.ie){
node.setAttribute(this[ns].alias+":"+attr,this[ns].prefix+_929);
}else{
node.setAttributeNS(this[ns]["namespace"],attr,this[ns].prefix+_929);
}
},getAttr:function(node,ns,attr){
if(dojo.render.html.ie){
return node.getAttribute(this[ns].alias+":"+attr);
}else{
return node.getAttributeNS(this[ns]["namespace"],attr);
}
},removeAttr:function(node,ns,attr){
var _930=true;
if(dojo.render.html.ie){
_930=node.removeAttribute(this[ns].alias+":"+attr);
}else{
node.removeAttributeNS(this[ns]["namespace"],attr);
}
return _930;
}};
dojo.widget.attachTemplateNodes=function(_931,_932,_933){
var _934=dojo.dom.ELEMENT_NODE;
function trim(str){
return str.replace(/^\s+|\s+$/g,"");
}
if(!_931){
_931=_932.domNode;
}
if(_931.nodeType!=_934){
return;
}
var _936=_931.all||_931.getElementsByTagName("*");
var _937=_932;
for(var x=-1;x<_936.length;x++){
var _939=(x==-1)?_931:_936[x];
var _93a=[];
if(!_932.widgetsInTemplate||!_939.getAttribute("dojoType")){
for(var y=0;y<this.attachProperties.length;y++){
var _93c=_939.getAttribute(this.attachProperties[y]);
if(_93c){
_93a=_93c.split(";");
for(var z=0;z<_93a.length;z++){
if(dojo.lang.isArray(_932[_93a[z]])){
_932[_93a[z]].push(_939);
}else{
_932[_93a[z]]=_939;
}
}
break;
}
}
var _93e=_939.getAttribute(this.eventAttachProperty);
if(_93e){
var evts=_93e.split(";");
for(var y=0;y<evts.length;y++){
if((!evts[y])||(!evts[y].length)){
continue;
}
var _940=null;
var tevt=trim(evts[y]);
if(evts[y].indexOf(":")>=0){
var _942=tevt.split(":");
tevt=trim(_942[0]);
_940=trim(_942[1]);
}
if(!_940){
_940=tevt;
}
var tf=function(){
var ntf=new String(_940);
return function(evt){
if(_937[ntf]){
_937[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_939,tevt,tf,false,true);
}
}
for(var y=0;y<_933.length;y++){
var _946=_939.getAttribute(_933[y]);
if((_946)&&(_946.length)){
var _940=null;
var _947=_933[y].substr(4);
_940=trim(_946);
var _948=[_940];
if(_940.indexOf(";")>=0){
_948=dojo.lang.map(_940.split(";"),trim);
}
for(var z=0;z<_948.length;z++){
if(!_948[z].length){
continue;
}
var tf=function(){
var ntf=new String(_948[z]);
return function(evt){
if(_937[ntf]){
_937[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_939,_947,tf,false,true);
}
}
}
}
var _94b=_939.getAttribute(this.templateProperty);
if(_94b){
_932[_94b]=_939;
}
dojo.lang.forEach(dojo.widget.waiNames,function(name){
var wai=dojo.widget.wai[name];
var val=_939.getAttribute(wai.name);
if(val){
if(val.indexOf("-")==-1){
dojo.widget.wai.setAttr(_939,wai.name,"role",val);
}else{
var _94f=val.split("-");
dojo.widget.wai.setAttr(_939,wai.name,_94f[0],_94f[1]);
}
}
},this);
var _950=_939.getAttribute(this.onBuildProperty);
if(_950){
eval("var node = baseNode; var widget = targetObj; "+_950);
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
},{templateNode:null,templateString:null,templateCssString:null,preventClobber:false,domNode:null,containerNode:null,widgetsInTemplate:false,addChild:function(_958,_959,pos,ref,_95c){
if(!this.isContainer){
dojo.debug("dojo.widget.DomWidget.addChild() attempted on non-container widget");
return null;
}else{
if(_95c==undefined){
_95c=this.children.length;
}
this.addWidgetAsDirectChild(_958,_959,pos,ref,_95c);
this.registerChild(_958,_95c);
}
return _958;
},addWidgetAsDirectChild:function(_95d,_95e,pos,ref,_961){
if((!this.containerNode)&&(!_95e)){
this.containerNode=this.domNode;
}
var cn=(_95e)?_95e:this.containerNode;
if(!pos){
pos="after";
}
if(!ref){
if(!cn){
cn=dojo.body();
}
ref=cn.lastChild;
}
if(!_961){
_961=0;
}
_95d.domNode.setAttribute("dojoinsertionindex",_961);
if(!ref){
cn.appendChild(_95d.domNode);
}else{
if(pos=="insertAtIndex"){
dojo.dom.insertAtIndex(_95d.domNode,ref.parentNode,_961);
}else{
if((pos=="after")&&(ref===cn.lastChild)){
cn.appendChild(_95d.domNode);
}else{
dojo.dom.insertAtPosition(_95d.domNode,cn,pos);
}
}
}
},registerChild:function(_963,_964){
_963.dojoInsertionIndex=_964;
var idx=-1;
for(var i=0;i<this.children.length;i++){
if(this.children[i].dojoInsertionIndex<=_964){
idx=i;
}
}
this.children.splice(idx+1,0,_963);
_963.parent=this;
_963.addedTo(this,idx+1);
delete dojo.widget.manager.topWidgets[_963.widgetId];
},removeChild:function(_967){
dojo.dom.removeNode(_967.domNode);
return dojo.widget.DomWidget.superclass.removeChild.call(this,_967);
},getFragNodeRef:function(frag){
if(!frag){
return null;
}
if(!frag[this.getNamespacedType()]){
dojo.raise("Error: no frag for widget type "+this.getNamespacedType()+", id "+this.widgetId+" (maybe a widget has set it's type incorrectly)");
}
return frag[this.getNamespacedType()]["nodeRef"];
},postInitialize:function(args,frag,_96b){
var _96c=this.getFragNodeRef(frag);
if(_96b&&(_96b.snarfChildDomOutput||!_96c)){
_96b.addWidgetAsDirectChild(this,"","insertAtIndex","",args["dojoinsertionindex"],_96c);
}else{
if(_96c){
if(this.domNode&&(this.domNode!==_96c)){
var _96d=_96c.parentNode.replaceChild(this.domNode,_96c);
}
}
}
if(_96b){
_96b.registerChild(this,args.dojoinsertionindex);
}else{
dojo.widget.manager.topWidgets[this.widgetId]=this;
}
if(this.widgetsInTemplate){
var _96e=new dojo.xml.Parse();
var _96f;
var _970=this.domNode.getElementsByTagName("*");
for(var i=0;i<_970.length;i++){
if(_970[i].getAttribute("dojoAttachPoint")=="subContainerWidget"){
_96f=_970[i];
}
if(_970[i].getAttribute("dojoType")){
_970[i].setAttribute("_isSubWidget",true);
}
}
if(this.isContainer&&!this.containerNode){
if(_96f){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,_96f);
frag["dojoDontFollow"]=true;
}
}else{
dojo.debug("No subContainerWidget node can be found in template file for widget "+this);
}
}
var _973=_96e.parseElement(this.domNode,null,true);
dojo.widget.getParser().createSubComponents(_973,this);
var _974=[];
var _975=[this];
var w;
while((w=_975.pop())){
for(var i=0;i<w.children.length;i++){
var _977=w.children[i];
if(_977._processedSubWidgets||!_977.extraArgs["_issubwidget"]){
continue;
}
_974.push(_977);
if(_977.isContainer){
_975.push(_977);
}
}
}
for(var i=0;i<_974.length;i++){
var _978=_974[i];
if(_978._processedSubWidgets){
dojo.debug("This should not happen: widget._processedSubWidgets is already true!");
return;
}
_978._processedSubWidgets=true;
if(_978.extraArgs["dojoattachevent"]){
var evts=_978.extraArgs["dojoattachevent"].split(";");
for(var j=0;j<evts.length;j++){
var _97b=null;
var tevt=dojo.string.trim(evts[j]);
if(tevt.indexOf(":")>=0){
var _97d=tevt.split(":");
tevt=dojo.string.trim(_97d[0]);
_97b=dojo.string.trim(_97d[1]);
}
if(!_97b){
_97b=tevt;
}
if(dojo.lang.isFunction(_978[tevt])){
dojo.event.kwConnect({srcObj:_978,srcFunc:tevt,targetObj:this,targetFunc:_97b});
}else{
alert(tevt+" is not a function in widget "+_978);
}
}
}
if(_978.extraArgs["dojoattachpoint"]){
this[_978.extraArgs["dojoattachpoint"]]=_978;
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
var _981=args["templateCssPath"]||this.templateCssPath;
if(_981&&!dojo.widget._cssFiles[_981.toString()]){
if((!this.templateCssString)&&(_981)){
this.templateCssString=dojo.hostenv.getText(_981);
this.templateCssPath=null;
}
dojo.widget._cssFiles[_981.toString()]=true;
}
if((this["templateCssString"])&&(!this.templateCssString["loaded"])){
dojo.html.insertCssText(this.templateCssString,null,_981);
if(!this.templateCssString){
this.templateCssString="";
}
this.templateCssString.loaded=true;
}
if((!this.preventClobber)&&((this.templatePath)||(this.templateNode)||((this["templateString"])&&(this.templateString.length))||((typeof ts!="undefined")&&((ts["string"])||(ts["node"]))))){
this.buildFromTemplate(args,frag);
}else{
this.domNode=this.getFragNodeRef(frag);
}
this.fillInTemplate(args,frag);
},buildFromTemplate:function(args,frag){
var _984=false;
if(args["templatepath"]){
args["templatePath"]=args["templatepath"];
}
dojo.widget.fillFromTemplateCache(this,args["templatePath"],null,_984);
var ts=dojo.widget._templateCache[this.templatePath?this.templatePath.toString():this.widgetType];
if((ts)&&(!_984)){
if(!this.templateString.length){
this.templateString=ts["string"];
}
if(!this.templateNode){
this.templateNode=ts["node"];
}
}
var _986=false;
var node=null;
var tstr=this.templateString;
if((!this.templateNode)&&(this.templateString)){
_986=this.templateString.match(/\$\{([^\}]+)\}/g);
if(_986){
var hash=this.strings||{};
for(var key in dojo.widget.defaultStrings){
if(dojo.lang.isUndefined(hash[key])){
hash[key]=dojo.widget.defaultStrings[key];
}
}
for(var i=0;i<_986.length;i++){
var key=_986[i];
key=key.substring(2,key.length-1);
var kval=(key.substring(0,5)=="this.")?dojo.lang.getObjPathValue(key.substring(5),this):hash[key];
var _98d;
if((kval)||(dojo.lang.isString(kval))){
_98d=new String((dojo.lang.isFunction(kval))?kval.call(this,key,this.templateString):kval);
while(_98d.indexOf("\"")>-1){
_98d=_98d.replace("\"","&quot;");
}
tstr=tstr.replace(_986[i],_98d);
}
}
}else{
this.templateNode=this.createNodesFromText(this.templateString,true)[0];
if(!_984){
ts.node=this.templateNode;
}
}
}
if((!this.templateNode)&&(!_986)){
dojo.debug("DomWidget.buildFromTemplate: could not create template");
return false;
}else{
if(!_986){
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
},attachTemplateNodes:function(_98f,_990){
if(!_98f){
_98f=this.domNode;
}
if(!_990){
_990=this;
}
return dojo.widget.attachTemplateNodes(_98f,_990,dojo.widget.getDojoEventsFromStr(this.templateString));
},fillInTemplate:function(){
},destroyRendering:function(){
try{
delete this.domNode;
}
catch(e){
}
},createNodesFromText:function(){
dojo.unimplemented("dojo.widget.DomWidget.createNodesFromText");
}});
dojo.provide("dojo.lfx.toggle");
dojo.lfx.toggle.plain={show:function(node,_992,_993,_994){
dojo.html.show(node);
if(dojo.lang.isFunction(_994)){
_994();
}
},hide:function(node,_996,_997,_998){
dojo.html.hide(node);
if(dojo.lang.isFunction(_998)){
_998();
}
}};
dojo.lfx.toggle.fade={show:function(node,_99a,_99b,_99c){
dojo.lfx.fadeShow(node,_99a,_99b,_99c).play();
},hide:function(node,_99e,_99f,_9a0){
dojo.lfx.fadeHide(node,_99e,_99f,_9a0).play();
}};
dojo.lfx.toggle.wipe={show:function(node,_9a2,_9a3,_9a4){
dojo.lfx.wipeIn(node,_9a2,_9a3,_9a4).play();
},hide:function(node,_9a6,_9a7,_9a8){
dojo.lfx.wipeOut(node,_9a6,_9a7,_9a8).play();
}};
dojo.lfx.toggle.explode={show:function(node,_9aa,_9ab,_9ac,_9ad){
dojo.lfx.explode(_9ad||{x:0,y:0,width:0,height:0},node,_9aa,_9ab,_9ac).play();
},hide:function(node,_9af,_9b0,_9b1,_9b2){
dojo.lfx.implode(node,_9b2||{x:0,y:0,width:0,height:0},_9af,_9b0,_9b1).play();
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
},destroyRendering:function(_9b9){
try{
if(!_9b9&&this.domNode){
dojo.event.browser.clean(this.domNode);
}
this.domNode.parentNode.removeChild(this.domNode);
delete this.domNode;
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
var _9bd=w||wh.width;
var _9be=h||wh.height;
if(this.width==_9bd&&this.height==_9be){
return false;
}
this.width=_9bd;
this.height=_9be;
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
dojo.lang.forEach(this.children,function(_9c1){
if(_9c1.checkSize){
_9c1.checkSize();
}
});
}});
dojo.provide("dojo.widget.*");
dojo.provide("dojo.widget.ContentPane");
dojo.widget.defineWidget("dojo.widget.ContentPane",dojo.widget.HtmlWidget,function(){
this._styleNodes=[];
this._onLoadStack=[];
this._onUnloadStack=[];
this._callOnUnload=false;
this._ioBindObj;
this.scriptScope;
this.bindArgs={};
},{isContainer:true,adjustPaths:true,href:"",extractContent:true,parseContent:true,cacheContent:true,preload:false,refreshOnShow:false,handler:"",executeScripts:false,scriptSeparation:true,loadingMessage:"Loading...",isLoaded:false,postCreate:function(args,frag,_9c4){
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
},_downloadExternalContent:function(url,_9c8){
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
}},_9c8));
},_cacheSetting:function(_9ce,_9cf){
for(var x in this.bindArgs){
if(dojo.lang.isUndefined(_9ce[x])){
_9ce[x]=this.bindArgs[x];
}
}
if(dojo.lang.isUndefined(_9ce.useCache)){
_9ce.useCache=_9cf;
}
if(dojo.lang.isUndefined(_9ce.preventCache)){
_9ce.preventCache=!_9cf;
}
if(dojo.lang.isUndefined(_9ce.mimetype)){
_9ce.mimetype="text/html";
}
return _9ce;
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
},_runStack:function(_9d4){
var st=this[_9d4];
var err="";
var _9d7=this.scriptScope||window;
for(var i=0;i<st.length;i++){
try{
st[i].call(_9d7);
}
catch(e){
err+="\n"+st[i]+" failed: "+e.description;
}
}
this[_9d4]=[];
if(err.length){
var name=(_9d4=="_onLoadStack")?"addOnLoad":"addOnUnLoad";
this._handleDefaults(name+" failure\n "+err,"onExecError","debug");
}
},addOnLoad:function(obj,func){
this._pushOnStack(this._onLoadStack,obj,func);
},addOnUnload:function(obj,func){
this._pushOnStack(this._onUnloadStack,obj,func);
},addOnUnLoad:function(){
dojo.deprecated(this.widgetType+".addOnUnLoad, use addOnUnload instead. (lowercased Load)",0.5);
this.addOnUnload.apply(this,arguments);
},_pushOnStack:function(_9de,obj,func){
if(typeof func=="undefined"){
_9de.push(obj);
}else{
_9de.push(function(){
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
},_handleDefaults:function(e,_9e8,_9e9){
if(!_9e8){
_9e8="onContentError";
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
this[_9e8](e);
if(e.returnValue){
switch(_9e9){
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
var _9ec=[],_9ed=[],tmp=[];
var _9ef=[],_9f0=[],attr=[],_9f2=[];
var str="",path="",fix="",_9f6="",tag="",_9f8="";
if(!url){
url="./";
}
if(s){
var _9f9=/<title[^>]*>([\s\S]*?)<\/title>/i;
while(_9ef=_9f9.exec(s)){
_9ec.push(_9ef[1]);
s=s.substring(0,_9ef.index)+s.substr(_9ef.index+_9ef[0].length);
}
if(this.adjustPaths){
var _9fa=/<[a-z][a-z0-9]*[^>]*\s(?:(?:src|href|style)=[^>])+[^>]*>/i;
var _9fb=/\s(src|href|style)=(['"]?)([\w()\[\]\/.,\\'"-:;#=&?\s@]+?)\2/i;
var _9fc=/^(?:[#]|(?:(?:https?|ftps?|file|javascript|mailto|news):))/;
while(tag=_9fa.exec(s)){
str+=s.substring(0,tag.index);
s=s.substring((tag.index+tag[0].length),s.length);
tag=tag[0];
_9f6="";
while(attr=_9fb.exec(tag)){
path="";
_9f8=attr[3];
switch(attr[1].toLowerCase()){
case "src":
case "href":
if(_9fc.exec(_9f8)){
path=_9f8;
}else{
path=(new dojo.uri.Uri(url,_9f8).toString());
}
break;
case "style":
path=dojo.html.fixPathsInCssText(_9f8,url);
break;
default:
path=_9f8;
}
fix=" "+attr[1]+"="+attr[2]+path+attr[2];
_9f6+=tag.substring(0,attr.index)+fix;
tag=tag.substring((attr.index+attr[0].length),tag.length);
}
str+=_9f6+tag;
}
s=str+s;
}
_9f9=/(?:<(style)[^>]*>([\s\S]*?)<\/style>|<link ([^>]*rel=['"]?stylesheet['"]?[^>]*)>)/i;
while(_9ef=_9f9.exec(s)){
if(_9ef[1]&&_9ef[1].toLowerCase()=="style"){
_9f2.push(dojo.html.fixPathsInCssText(_9ef[2],url));
}else{
if(attr=_9ef[3].match(/href=(['"]?)([^'">]*)\1/i)){
_9f2.push({path:attr[2]});
}
}
s=s.substring(0,_9ef.index)+s.substr(_9ef.index+_9ef[0].length);
}
var _9f9=/<script([^>]*)>([\s\S]*?)<\/script>/i;
var _9fd=/src=(['"]?)([^"']*)\1/i;
var _9fe=/.*(\bdojo\b\.js(?:\.uncompressed\.js)?)$/;
var _9ff=/(?:var )?\bdjConfig\b(?:[\s]*=[\s]*\{[^}]+\}|\.[\w]*[\s]*=[\s]*[^;\n]*)?;?|dojo\.hostenv\.writeIncludes\(\s*\);?/g;
var _a00=/dojo\.(?:(?:require(?:After)?(?:If)?)|(?:widget\.(?:manager\.)?registerWidgetPackage)|(?:(?:hostenv\.)?setModulePrefix|registerModulePath)|defineNamespace)\((['"]).*?\1\)\s*;?/;
while(_9ef=_9f9.exec(s)){
if(this.executeScripts&&_9ef[1]){
if(attr=_9fd.exec(_9ef[1])){
if(_9fe.exec(attr[2])){
dojo.debug("Security note! inhibit:"+attr[2]+" from  being loaded again.");
}else{
_9ed.push({path:attr[2]});
}
}
}
if(_9ef[2]){
var sc=_9ef[2].replace(_9ff,"");
if(!sc){
continue;
}
while(tmp=_a00.exec(sc)){
_9f0.push(tmp[0]);
sc=sc.substring(0,tmp.index)+sc.substr(tmp.index+tmp[0].length);
}
if(this.executeScripts){
_9ed.push(sc);
}
}
s=s.substr(0,_9ef.index)+s.substr(_9ef.index+_9ef[0].length);
}
if(this.extractContent){
_9ef=s.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_9ef){
s=_9ef[1];
}
}
if(this.executeScripts&&this.scriptSeparation){
var _9f9=/(<[a-zA-Z][a-zA-Z0-9]*\s[^>]*?\S=)((['"])[^>]*scriptScope[^>]*>)/;
var _a02=/([\s'";:\(])scriptScope(.*)/;
str="";
while(tag=_9f9.exec(s)){
tmp=((tag[3]=="'")?"\"":"'");
fix="";
str+=s.substring(0,tag.index)+tag[1];
while(attr=_a02.exec(tag[2])){
tag[2]=tag[2].substring(0,attr.index)+attr[1]+"dojo.widget.byId("+tmp+this.widgetId+tmp+").scriptScope"+attr[2];
}
str+=tag[2];
s=s.substr(tag.index+tag[0].length);
}
s=str+s;
}
}
return {"xml":s,"styles":_9f2,"titles":_9ec,"requires":_9f0,"scripts":_9ed,"url":url};
},_setContent:function(cont){
this.destroyChildren();
for(var i=0;i<this._styleNodes.length;i++){
if(this._styleNodes[i]&&this._styleNodes[i].parentNode){
this._styleNodes[i].parentNode.removeChild(this._styleNodes[i]);
}
}
this._styleNodes=[];
var node=this.containerNode||this.domNode;
while(node.firstChild){
try{
dojo.event.browser.clean(node.firstChild);
}
catch(e){
}
node.removeChild(node.firstChild);
}
try{
if(typeof cont!="string"){
node.innerHTML="";
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
this._styleNodes.push(dojo.html.insertCssFile(data.styles[i].path));
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
var _a08=this;
function asyncParse(){
if(_a08.executeScripts){
_a08._executeScripts(data.scripts);
}
if(_a08.parseContent){
var node=_a08.containerNode||_a08.domNode;
var _a0a=new dojo.xml.Parse();
var frag=_a0a.parseElement(node,null,true);
dojo.widget.getParser().createSubComponents(frag,_a08);
}
_a08.onResized();
_a08.onLoad();
}
if(dojo.hostenv.isXDomain&&data.requires.length){
dojo.addOnLoad(asyncParse);
}else{
asyncParse();
}
}
},setHandler:function(_a0c){
var fcn=dojo.lang.isFunction(_a0c)?_a0c:window[_a0c];
if(!dojo.lang.isFunction(fcn)){
this._handleDefaults("Unable to set handler, '"+_a0c+"' not a function.","onExecError",true);
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
},_executeScripts:function(_a0f){
var self=this;
var tmp="",code="";
for(var i=0;i<_a0f.length;i++){
if(_a0f[i].path){
dojo.io.bind(this._cacheSetting({"url":_a0f[i].path,"load":function(type,_a15){
dojo.lang.hitch(self,tmp=";"+_a15);
},"error":function(type,_a17){
_a17.text=type+" downloading remote script";
self._handleDefaults.call(self,_a17,"onExecError","debug");
},"mimetype":"text/plain","sync":true},this.cacheContent));
code+=tmp;
}else{
code+=_a0f[i];
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
dojo.widget.html.layout=function(_a1b,_a1c,_a1d){
dojo.html.addClass(_a1b,"dojoLayoutContainer");
_a1c=dojo.lang.filter(_a1c,function(_a1e,idx){
_a1e.idx=idx;
return dojo.lang.inArray(["top","bottom","left","right","client","flood"],_a1e.layoutAlign);
});
if(_a1d&&_a1d!="none"){
var rank=function(_a21){
switch(_a21.layoutAlign){
case "flood":
return 1;
case "left":
case "right":
return (_a1d=="left-right")?2:3;
case "top":
case "bottom":
return (_a1d=="left-right")?3:2;
default:
return 4;
}
};
_a1c.sort(function(a,b){
return (rank(a)-rank(b))||(a.idx-b.idx);
});
}
var f={top:dojo.html.getPixelValue(_a1b,"padding-top",true),left:dojo.html.getPixelValue(_a1b,"padding-left",true)};
dojo.lang.mixin(f,dojo.html.getContentBox(_a1b));
dojo.lang.forEach(_a1c,function(_a25){
var elm=_a25.domNode;
var pos=_a25.layoutAlign;
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
}else{
if(pos=="left"||pos=="right"){
var w=dojo.html.getMarginBox(elm).width;
dojo.html.setMarginBox(elm,{width:w,height:f.height});
f.width-=w;
if(pos=="left"){
f.left+=w;
}else{
elm.style.left=f.left+f.width+"px";
}
}else{
if(pos=="flood"||pos=="client"){
dojo.html.setMarginBox(elm,{width:f.width,height:f.height});
}
}
}
if(_a25.onResized){
_a25.onResized();
}
});
};
dojo.html.insertCssText(".dojoLayoutContainer{ position: relative; display: block; }\n"+"body .dojoAlignTop, body .dojoAlignBottom, body .dojoAlignLeft, body .dojoAlignRight { position: absolute; overflow: hidden; }\n"+"body .dojoAlignClient { position: absolute }\n"+".dojoAlignClient { overflow: auto; }\n");
dojo.provide("dojo.widget.LayoutContainer");
dojo.widget.defineWidget("dojo.widget.LayoutContainer",dojo.widget.HtmlWidget,{isContainer:true,layoutChildPriority:"top-bottom",postCreate:function(){
dojo.widget.html.layout(this.domNode,this.children,this.layoutChildPriority);
},addChild:function(_a2a,_a2b,pos,ref,_a2e){
dojo.widget.LayoutContainer.superclass.addChild.call(this,_a2a,_a2b,pos,ref,_a2e);
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
dojo.declare("dojo.widget.ModalDialogBase",null,{isContainer:true,shared:{bg:null,bgIframe:null},focusElement:"",bgColor:"black",bgOpacity:0.4,followScroll:true,trapTabs:function(e){
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
var _a32=this;
setTimeout(function(){
_a32._fromTrap=false;
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
if(!this.shared.bg){
this.shared.bg=document.createElement("div");
this.shared.bg.className="dialogUnderlay";
with(this.shared.bg.style){
position="absolute";
left=top="0px";
zIndex=998;
display="none";
}
this.setBackgroundColor(this.bgColor);
b.appendChild(this.shared.bg);
this.shared.bgIframe=new dojo.html.BackgroundIframe(this.shared.bg);
}
},setBackgroundColor:function(_a34){
if(arguments.length>=3){
_a34=new dojo.gfx.color.Color(arguments[0],arguments[1],arguments[2]);
}else{
_a34=new dojo.gfx.color.Color(_a34);
}
this.shared.bg.style.backgroundColor=_a34.toString();
return this.bgColor=_a34;
},setBackgroundOpacity:function(op){
if(arguments.length==0){
op=this.bgOpacity;
}
dojo.html.setOpacity(this.shared.bg,op);
try{
this.bgOpacity=dojo.html.getOpacity(this.shared.bg);
}
catch(e){
this.bgOpacity=op;
}
return this.bgOpacity;
},_sizeBackground:function(){
if(this.bgOpacity>0){
var _a36=dojo.html.getViewport();
var h=_a36.height;
var w=_a36.width;
with(this.shared.bg.style){
width=w+"px";
height=h+"px";
}
var _a39=dojo.html.getScroll().offset;
this.shared.bg.style.top=_a39.y+"px";
this.shared.bg.style.left=_a39.x+"px";
var _a36=dojo.html.getViewport();
if(_a36.width!=w){
this.shared.bg.style.width=_a36.width+"px";
}
if(_a36.height!=h){
this.shared.bg.style.height=_a36.height+"px";
}
}
},_showBackground:function(){
if(this.bgOpacity>0){
this.shared.bg.style.display="block";
}
},placeModalDialog:function(){
var _a3a=dojo.html.getScroll().offset;
var _a3b=dojo.html.getViewport();
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
var x=_a3a.x+(_a3b.width-mb.width)/2;
var y=_a3a.y+(_a3b.height-mb.height)/2;
with(this.domNode.style){
left=x+"px";
top=y+"px";
}
},showModalDialog:function(){
if(this.followScroll&&!this._scrollConnected){
this._scrollConnected=true;
dojo.event.connect(window,"onscroll",this,"_onScroll");
}
this.placeModalDialog();
this.setBackgroundOpacity();
this._sizeBackground();
this._showBackground();
},hideModalDialog:function(){
if(this.focusElement){
dojo.byId(this.focusElement).focus();
dojo.byId(this.focusElement).blur();
}
this.shared.bg.style.display="none";
this.shared.bg.style.width=this.shared.bg.style.height="1px";
if(this._scrollConnected){
this._scrollConnected=false;
dojo.event.disconnect(window,"onscroll",this,"_onScroll");
}
},_onScroll:function(){
var _a3f=dojo.html.getScroll().offset;
this.shared.bg.style.top=_a3f.y+"px";
this.shared.bg.style.left=_a3f.x+"px";
this.placeModalDialog();
},checkSize:function(){
if(this.isShowing()){
this._sizeBackground();
this.placeModalDialog();
this.onResized();
}
}});
dojo.widget.defineWidget("dojo.widget.Dialog",[dojo.widget.ContentPane,dojo.widget.ModalDialogBase],{templateString:"<div id=\"${this.widgetId}\" class=\"dojoDialog\" dojoattachpoint=\"wrapper\">\n	<span dojoattachpoint=\"tabStartOuter\" dojoonfocus=\"trapTabs\" dojoonblur=\"clearTrap\"	tabindex=\"0\"></span>\n	<span dojoattachpoint=\"tabStart\" dojoonfocus=\"trapTabs\" dojoonblur=\"clearTrap\" tabindex=\"0\"></span>\n	<div dojoattachpoint=\"containerNode\" style=\"position: relative; z-index: 2;\"></div>\n	<span dojoattachpoint=\"tabEnd\" dojoonfocus=\"trapTabs\" dojoonblur=\"clearTrap\" tabindex=\"0\"></span>\n	<span dojoattachpoint=\"tabEndOuter\" dojoonfocus=\"trapTabs\" dojoonblur=\"clearTrap\" tabindex=\"0\"></span>\n</div>\n",blockDuration:0,lifetime:0,show:function(){
if(this.lifetime){
this.timeRemaining=this.lifetime;
if(!this.blockDuration){
dojo.event.connect(this.shared.bg,"onclick",this,"hide");
}else{
dojo.event.disconnect(this.shared.bg,"onclick",this,"hide");
}
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
this.closeNode=node;
dojo.event.connect(node,"onclick",this,"hide");
},setShowControl:function(node){
dojo.event.connect(node,"onclick",this,"show");
},_onTick:function(){
if(this.timer){
this.timeRemaining-=100;
if(this.lifetime-this.timeRemaining>=this.blockDuration){
dojo.event.connect(this.shared.bg,"onclick",this,"hide");
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

