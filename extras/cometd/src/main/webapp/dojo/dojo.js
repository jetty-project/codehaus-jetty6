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
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 5297 $".match(/[0-9]+/)[0]),toString:function(){
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
dojo.evalObjPath=function(_d,_e){
if(typeof _d!="string"){
return dojo.global();
}
if(_d.indexOf(".")==-1){
return dojo.evalProp(_d,dojo.global(),_e);
}
var _f=dojo.parseObjPath(_d,dojo.global(),_e);
if(_f){
return dojo.evalProp(_f.prop,_f.obj,_e);
}
return null;
};
dojo.errorToString=function(_10){
if(!dj_undef("message",_10)){
return _10.message;
}else{
if(!dj_undef("description",_10)){
return _10.description;
}else{
return _10;
}
}
};
dojo.raise=function(_11,_12){
if(_12){
_11=_11+": "+dojo.errorToString(_12);
}
try{
dojo.hostenv.println("FATAL: "+_11);
}
catch(e){
}
throw Error(_11);
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
function dj_eval(_14){
return dj_global.eval?dj_global.eval(_14):eval(_14);
}
dojo.unimplemented=function(_15,_16){
var _17="'"+_15+"' not implemented";
if(_16!=null){
_17+=" "+_16;
}
dojo.raise(_17);
};
dojo.deprecated=function(_18,_19,_1a){
var _1b="DEPRECATED: "+_18;
if(_19){
_1b+=" "+_19;
}
if(_1a){
_1b+=" -- will be removed in version: "+_1a;
}
dojo.debug(_1b);
};
dojo.render=(function(){
function vscaffold(_1c,_1d){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1c};
for(var i=0;i<_1d.length;i++){
tmp[_1d[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _20={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_20;
}else{
for(var _21 in _20){
if(typeof djConfig[_21]=="undefined"){
djConfig[_21]=_20[_21];
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
var _24=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _25={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_26,_27){
this.modulePrefixes_[_26]={name:_26,value:_27};
},moduleHasPrefix:function(_28){
var mp=this.modulePrefixes_;
return Boolean((mp[_28])&&(mp[_28]["value"]));
},getModulePrefix:function(_2a){
var mp=this.modulePrefixes_;
if((mp[_2a])&&(mp[_2a]["value"])){
return mp[_2a].value;
}
return _2a;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _25){
dojo.hostenv[_2c]=_25[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if((_2d.charAt(0)=="/")||(_2d.match(/^\w+:/))){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return ((!_2e)?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb));
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return 1;
}
var _33=this.getText(uri,null,true);
if(_33==null){
return 0;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return 1;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return ((ok)&&(this.findModule(_36,false)))?true:false;
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
if((this.loadUriStack.length==0)&&(this.getTextStack.length==0)){
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
if((i==1)&&(!this.moduleHasPrefix(_45))){
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
var _4e=((_4d[0].charAt(0)!="/")&&(!_4d[0].match(/^\w+:/)));
var _4f=_4d[_4d.length-1];
if(_4f=="*"){
_47=(_4c.slice(0,-1)).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&(_4b.charAt(0)=="/")){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,((!_49)?_47:null));
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var ok=this.loadPath(_4b,((!_49)?_47:null));
if((!ok)&&(!_48)){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,((!_49)?_47:null));
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&(_4b.charAt(0)=="/")){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,((!_49)?_47:null));
if(ok){
break;
}
}
}
if((!ok)&&(!_49)){
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
dojo.hostenv.startPackage=function(_51){
var _52=(new String(_51)).toString();
var _53=_52;
var _54=_51.split(/\./);
if(_54[_54.length-1]=="*"){
_54.pop();
_53=_54.join(".");
}
var _55=dojo.evalObjPath(_53.toString(),true);
this.loaded_modules_[_52]=_55;
this.loaded_modules_[_53]=_55;
return _55;
};
dojo.hostenv.findModule=function(_56,_57){
var lmn=String(_56).toString();
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_57){
dojo.raise("no loaded module named '"+_56+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_59){
var _5a=_59["common"]||[];
var _5b=(_59[dojo.hostenv.name_])?_5a.concat(_59[dojo.hostenv.name_]||[]):_5a.concat(_59["default"]||[]);
for(var x=0;x<_5b.length;x++){
var _5d=_5b[x];
if(_5d.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5d);
}else{
dojo.hostenv.loadModule(_5d);
}
}
};
dojo.require=function(){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(){
if((arguments[0]===true)||(arguments[0]=="common")||(arguments[0]&&dojo.render[arguments[0]].capable)){
var _5e=[];
for(var i=1;i<arguments.length;i++){
_5e.push(arguments[i]);
}
dojo.require.apply(dojo,_5e);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_60,_61){
return dojo.hostenv.setModulePrefix(_60,_61);
};
dojo.setModulePrefix=function(_62,_63){
dojo.deprecated("dojo.setModulePrefix(\""+_62+"\", \""+_63+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_62,_63);
};
dojo.exists=function(obj,_65){
var p=_65.split(".");
for(var i=0;i<p.length;i++){
if(!(obj[p[i]])){
return false;
}
obj=obj[p[i]];
}
return true;
};
}
if(typeof window=="undefined"){
dojo.raise("no window object");
}
(function(){
if(djConfig.allowQueryConfig){
var _68=document.location.toString();
var _69=_68.split("?",2);
if(_69.length>1){
var _6a=_69[1];
var _6b=_6a.split("&");
for(var x in _6b){
var sp=_6b[x].split("=");
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
var _6f=document.getElementsByTagName("script");
var _70=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_6f.length;i++){
var src=_6f[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_70);
if(m){
var _74=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_74+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_74;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_74;
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
var _7c=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_7c>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_7c+6,_7c+14);
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
var _7e=window["document"];
var tdi=_7e["implementation"];
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
var _82=null;
var _83=null;
try{
_82=new XMLHttpRequest();
}
catch(e){
}
if(!_82){
for(var i=0;i<3;++i){
var _85=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_82=new ActiveXObject(_85);
}
catch(e){
_83=e;
}
if(_82){
dojo.hostenv._XMLHTTP_PROGIDS=[_85];
break;
}
}
}
if(!_82){
return dojo.raise("XMLHTTP not available",_83);
}
return _82;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_87,_88){
if(!_87){
this._blockAsync=true;
}
var _89=this.getXmlhttpObject();
function isDocumentOk(_8a){
var _8b=_8a["status"];
return Boolean((!_8b)||((200<=_8b)&&(300>_8b))||(_8b==304));
}
if(_87){
var _8c=this,timer=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_89.onreadystatechange=function(){
if(timer){
gbl.clearTimeout(timer);
timer=null;
}
if(_8c._blockAsync||(xhr&&xhr._blockAsync)){
timer=gbl.setTimeout(function(){
_89.onreadystatechange.apply(this);
},10);
}else{
if(4==_89.readyState){
if(isDocumentOk(_89)){
_87(_89.responseText);
}
}
}
};
}
_89.open("GET",uri,_87?true:false);
try{
_89.send(null);
if(_87){
return null;
}
if(!isDocumentOk(_89)){
var err=Error("Unable to load "+uri+" status:"+_89.status);
err.status=_89.status;
err.responseText=_89.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_88)&&(!_87)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _89.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_8f){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_8f);
}else{
try{
var _90=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_90){
_90=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_8f));
_90.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_8f+"</div>");
}
catch(e2){
window.status=_8f;
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
function dj_addNodeEvtHdlr(_92,_93,fp,_95){
var _96=_92["on"+_93]||function(){
};
_92["on"+_93]=function(){
fp.apply(_92,arguments);
_96.apply(_92,arguments);
};
return true;
}
function dj_load_init(e){
var _98=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_98!="domcontentloaded"&&_98!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _99=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_99();
dojo.hostenv.modulesLoaded();
}else{
dojo.addOnLoad(_99);
}
}
if(document.addEventListener){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
document.addEventListener("load",dj_load_init,null);
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
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
});
dojo.hostenv.makeWidgets=function(){
var _9b=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_9b=_9b.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_9b=_9b.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_9b.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _9c=new dojo.xml.Parse();
if(_9b.length>0){
for(var x=0;x<_9b.length;x++){
var _9e=document.getElementById(_9b[x]);
if(!_9e){
continue;
}
var _9f=_9c.parseElement(_9e,null,true);
dojo.widget.getParser().createComponents(_9f);
}
}else{
if(djConfig.parseWidgets){
var _9f=_9c.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_9f);
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
return (doc||dj_currentDocument).getElementById(id);
}
return id;
};
dojo.setContext=function(_a2,_a3){
dj_currentContext=_a2;
dj_currentDocument=_a3;
};
dojo._fireCallback=function(_a4,_a5,_a6){
if((_a5)&&((typeof _a4=="string")||(_a4 instanceof String))){
_a4=_a5[_a4];
}
return (_a5?_a4.apply(_a5,_a6||[]):_a4());
};
dojo.withGlobal=function(_a7,_a8,_a9,_aa){
var _ab;
var _ac=dj_currentContext;
var _ad=dj_currentDocument;
try{
dojo.setContext(_a7,_a7.document);
_ab=dojo._fireCallback(_a8,_a9,_aa);
}
finally{
dojo.setContext(_ac,_ad);
}
return _ab;
};
dojo.withDoc=function(_ae,_af,_b0,_b1){
var _b2;
var _b3=dj_currentDocument;
try{
dj_currentDocument=_ae;
_b2=dojo._fireCallback(_af,_b0,_b1);
}
finally{
dj_currentDocument=_b3;
}
return _b2;
};
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _b4=false;
var _b5=false;
var _b6=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_b4=true;
}else{
if(typeof this["load"]=="function"){
_b5=true;
}else{
if(window.widget){
_b6=true;
}
}
}
var _b7=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_b7.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_b4)&&(!_b6)){
_b7.push("browser_debug.js");
}
if((this["djConfig"])&&(djConfig["compat"])){
_b7.push("compat/"+djConfig["compat"]+".js");
}
var _b8=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_b8=djConfig["baseLoaderUri"];
}
for(var x=0;x<_b7.length;x++){
var _ba=_b8+"src/"+_b7[x];
if(_b4||_b5){
load(_ba);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_ba+"'></scr"+"ipt>");
}
catch(e){
var _bb=document.createElement("script");
_bb.src=_ba;
document.getElementsByTagName("head")[0].appendChild(_bb);
}
}
}
})();
dojo.normalizeLocale=function(_bc){
return _bc?_bc.toLowerCase():dojo.locale;
};
dojo.searchLocalePath=function(_bd,_be,_bf){
_bd=dojo.normalizeLocale(_bd);
var _c0=_bd.split("-");
var _c1=[];
for(var i=_c0.length;i>0;i--){
_c1.push(_c0.slice(0,i).join("-"));
}
_c1.push(false);
if(_be){
_c1.reverse();
}
for(var j=_c1.length-1;j>=0;j--){
var loc=_c1[j]||"ROOT";
var _c5=_bf(loc);
if(_c5){
break;
}
}
};
dojo.requireLocalization=function(_c6,_c7,_c8){
var _c9=[_c6,"_nls",_c7].join(".");
var _ca=dojo.hostenv.startPackage(_c9);
dojo.hostenv.loaded_modules_[_c9]=_ca;
if(!dj_undef("dj_localesBuilt",dj_global)&&dojo.hostenv.loaded_modules_[_c9]){
_c8=dojo.normalizeLocale(_c8);
for(var i=0;i<dj_localesBuilt.length;i++){
if(dj_localesBuilt[i]==_c8){
return;
}
}
}
var _cc=dojo.hostenv.getModuleSymbols(_c6);
var _cd=_cc.concat("nls").join("/");
var _ce=false;
dojo.searchLocalePath(_c8,false,function(loc){
var pkg=_c9+"."+loc;
var _d1=false;
if(!dojo.hostenv.findModule(pkg)){
dojo.hostenv.loaded_modules_[pkg]=null;
var _d2=[_cd];
if(loc!="ROOT"){
_d2.push(loc);
}
_d2.push(_c7);
var _d3=_d2.join("/")+".js";
_d1=dojo.hostenv.loadPath(_d3,null,function(_d4){
var _d5=function(){
};
_d5.prototype=_ce;
_ca[loc]=new _d5();
for(var j in _d4){
_ca[loc][j]=_d4[j];
}
});
}else{
_d1=true;
}
if(_d1&&_ca[loc]){
_ce=_ca[loc];
}
});
};
(function(){
function preload(_d7){
if(!dj_undef("dj_localesGenerated",dj_global)){
dojo.setModulePrefix("nls","nls");
_d7=dojo.normalizeLocale(_d7);
dojo.searchLocalePath(_d7,true,function(loc){
for(var i=0;i<dj_localesGenerated.length;i++){
if(dj_localesGenerated[i]==loc){
dojo.require("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
}
preload(dojo.locale);
var _da=djConfig.extraLocale;
if(_da){
if(!_da instanceof Array){
_da=[_da];
}
for(var i=0;i<_da.length;i++){
preload(_da[i]);
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_df){
req(m,b,_df);
if(_df){
return;
}
for(var i=0;i<_da.length;i++){
req(m,b,_da[i]);
}
};
}
})();
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
dojo.string.repeat=function(str,_e7,_e8){
var out="";
for(var i=0;i<_e7;i++){
out+=str;
if(_e8&&i<_e7-1){
out+=_e8;
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
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_f6,_f7){
if(typeof _f7!="function"){
dojo.raise("dojo.inherits: superclass argument ["+_f7+"] must be a function (subclass: ["+_f6+"']");
}
_f6.prototype=new _f7();
_f6.prototype.constructor=_f6;
_f6.superclass=_f7.prototype;
_f6["super"]=_f7.prototype;
};
dojo.lang._mixin=function(obj,_f9){
var _fa={};
for(var x in _f9){
if((typeof _fa[x]=="undefined")||(_fa[x]!=_f9[x])){
obj[x]=_f9[x];
}
}
if(dojo.render.html.ie&&(typeof (_f9["toString"])=="function")&&(_f9["toString"]!=obj["toString"])&&(_f9["toString"]!=_fa["toString"])){
obj.toString=_f9.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_fd){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_ff,_100){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_ff.prototype,arguments[i]);
}
return _ff;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_102,_103,_104,_105){
if(!dojo.lang.isArrayLike(_102)&&dojo.lang.isArrayLike(_103)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_102;
_102=_103;
_103=temp;
}
var _107=dojo.lang.isString(_102);
if(_107){
_102=_102.split("");
}
if(_105){
var step=-1;
var i=_102.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_102.length;
}
if(_104){
while(i!=end){
if(_102[i]===_103){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_102[i]==_103){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_10b,_10c,_10d){
return dojo.lang.find(_10b,_10c,_10d,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_10e,_10f){
return dojo.lang.find(_10e,_10f)>-1;
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
return (it instanceof Function||typeof it=="function");
};
dojo.lang.isString=function(it){
return (it instanceof String||typeof it=="string");
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
dojo.lang.setTimeout=function(func,_11a){
var _11b=window,argsStart=2;
if(!dojo.lang.isFunction(func)){
_11b=func;
func=_11a;
_11a=arguments[2];
argsStart++;
}
if(dojo.lang.isString(func)){
func=_11b[func];
}
var args=[];
for(var i=argsStart;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_11b,args);
},_11a);
};
dojo.lang.clearTimeout=function(_11e){
dojo.global().clearTimeout(_11e);
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
dojo.lang.getObjPathValue=function(_126,_127,_128){
with(dojo.parseObjPath(_126,_127,_128)){
return dojo.evalProp(prop,obj,_128);
}
};
dojo.lang.setObjPathValue=function(_129,_12a,_12b,_12c){
if(arguments.length<4){
_12c=true;
}
with(dojo.parseObjPath(_129,_12b,_12c)){
if(obj&&(_12c||(prop in obj))){
obj[prop]=_12a;
}
}
};
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_12e,_12f,_130){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_12e){
this.mimetype=_12e;
}
if(_12f){
this.transport=_12f;
}
if(arguments.length>=4){
this.changeUrl=_130;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,evt){
},error:function(type,_135){
},timeout:function(type){
},handle:function(){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_137){
if(_137["url"]){
_137.url=_137.url.toString();
}
if(_137["formNode"]){
_137.formNode=dojo.byId(_137.formNode);
}
if(!_137["method"]&&_137["formNode"]&&_137["formNode"].method){
_137.method=_137["formNode"].method;
}
if(!_137["handle"]&&_137["handler"]){
_137.handle=_137.handler;
}
if(!_137["load"]&&_137["loaded"]){
_137.load=_137.loaded;
}
if(!_137["changeUrl"]&&_137["changeURL"]){
_137.changeUrl=_137.changeURL;
}
_137.encoding=dojo.lang.firstValued(_137["encoding"],djConfig["bindEncoding"],"");
_137.sendTransport=dojo.lang.firstValued(_137["sendTransport"],djConfig["ioSendTransport"],false);
var _138=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_137[fn]&&_138(_137[fn])){
continue;
}
if(_137["handle"]&&_138(_137["handle"])){
_137[fn]=_137.handle;
}
}
dojo.lang.mixin(this,_137);
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
dojo.io.bind=function(_13f){
if(!(_13f instanceof dojo.io.Request)){
try{
_13f=new dojo.io.Request(_13f);
}
catch(e){
dojo.debug(e);
}
}
var _140="";
if(_13f["transport"]){
_140=_13f["transport"];
if(!this[_140]){
return _13f;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_13f))){
_140=tmp;
}
}
if(_140==""){
return _13f;
}
}
this[_140].bind(_13f);
_13f.bindSuccess=true;
return _13f;
};
dojo.io.queueBind=function(_143){
if(!(_143 instanceof dojo.io.Request)){
try{
_143=new dojo.io.Request(_143);
}
catch(e){
dojo.debug(e);
}
}
var _144=_143.load;
_143.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_144.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _146=_143.error;
_143.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_146.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_143);
dojo.io._dispatchNextQueueBind();
return _143;
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
dojo.io.argsFromMap=function(map,_149,last){
var enc=/utf/i.test(_149||"")?encodeURIComponent:dojo.string.encodeAscii;
var _14c=[];
var _14d=new Object();
for(var name in map){
var _14f=function(elt){
var val=enc(name)+"="+enc(elt);
_14c[(last==name)?"push":"unshift"](val);
};
if(!_14d[name]){
var _152=map[name];
if(dojo.lang.isArray(_152)){
dojo.lang.forEach(_152,_14f);
}else{
_14f(_152);
}
}
}
return _14c.join("&");
};
dojo.io.setIFrameSrc=function(_153,src,_155){
try{
var r=dojo.render.html;
if(!_155){
if(r.safari){
_153.location=src;
}else{
frames[_153.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_153.contentWindow.document;
}else{
if(r.safari){
idoc=_153.document;
}else{
idoc=_153.contentWindow;
}
}
if(!idoc){
_153.location=src;
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
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_158,_159){
var fcn=(dojo.lang.isString(_159)?_158[_159]:_159)||function(){
};
return function(){
return fcn.apply(_158,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_15b,_15c,_15d){
var nso=(_15c||dojo.lang.anon);
if((_15d)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_15b){
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
nso[ret]=_15b;
return ret;
};
dojo.lang.forward=function(_161){
return function(){
return this[_161].apply(this,arguments);
};
};
dojo.lang.curry=function(ns,func){
var _164=[];
ns=ns||dj_global;
if(dojo.lang.isString(func)){
func=ns[func];
}
for(var x=2;x<arguments.length;x++){
_164.push(arguments[x]);
}
var _166=(func["__preJoinArity"]||func.length)-_164.length;
function gather(_167,_168,_169){
var _16a=_169;
var _16b=_168.slice(0);
for(var x=0;x<_167.length;x++){
_16b.push(_167[x]);
}
_169=_169-_167.length;
if(_169<=0){
var res=func.apply(ns,_16b);
_169=_16a;
return res;
}else{
return function(){
return gather(arguments,_16b,_169);
};
}
}
return gather([],_164,_166);
};
dojo.lang.curryArguments=function(ns,func,args,_171){
var _172=[];
var x=_171||0;
for(x=_171;x<args.length;x++){
_172.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[ns,func].concat(_172));
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
dojo.lang.delayThese=function(farr,cb,_178,_179){
if(!farr.length){
if(typeof _179=="function"){
_179();
}
return;
}
if((typeof _178=="undefined")&&(typeof cb=="number")){
_178=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_178){
_178=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_178,_179);
},_178);
};
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_17a){
this.pairs=[];
this.returnWrappers=_17a||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_17c,wrap,_17e,_17f){
var type=(_17f)?"unshift":"push";
this.pairs[type]([name,_17c,wrap,_17e]);
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
dojo.provide("dojo.lang.array");
dojo.lang.has=function(obj,name){
try{
return (typeof obj[name]!="undefined");
}
catch(e){
return false;
}
};
dojo.lang.isEmpty=function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _18a=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_18a++;
break;
}
}
return (_18a==0);
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
};
dojo.lang.map=function(arr,obj,_18e){
var _18f=dojo.lang.isString(arr);
if(_18f){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_18e)){
_18e=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_18e){
var _190=obj;
obj=_18e;
_18e=_190;
}
}
if(Array.map){
var _191=Array.map(arr,_18e,obj);
}else{
var _191=[];
for(var i=0;i<arr.length;++i){
_191.push(_18e.call(obj,arr[i]));
}
}
if(_18f){
return _191.join("");
}else{
return _191;
}
};
dojo.lang.reduce=function(arr,_194,obj,_196){
var _197=_194;
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_197=_196.call(ob,_197,val);
});
return _197;
};
dojo.lang.forEach=function(_19a,_19b,_19c){
if(dojo.lang.isString(_19a)){
_19a=_19a.split("");
}
if(Array.forEach){
Array.forEach(_19a,_19b,_19c);
}else{
if(!_19c){
_19c=dj_global;
}
for(var i=0,l=_19a.length;i<l;i++){
_19b.call(_19c,_19a[i],i,_19a);
}
}
};
dojo.lang._everyOrSome=function(_19e,arr,_1a0,_1a1){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[(_19e)?"every":"some"](arr,_1a0,_1a1);
}else{
if(!_1a1){
_1a1=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _1a3=_1a0.call(_1a1,arr[i],i,arr);
if((_19e)&&(!_1a3)){
return false;
}else{
if((!_19e)&&(_1a3)){
return true;
}
}
}
return (_19e)?true:false;
}
};
dojo.lang.every=function(arr,_1a5,_1a6){
return this._everyOrSome(true,arr,_1a5,_1a6);
};
dojo.lang.some=function(arr,_1a8,_1a9){
return this._everyOrSome(false,arr,_1a8,_1a9);
};
dojo.lang.filter=function(arr,_1ab,_1ac){
var _1ad=dojo.lang.isString(arr);
if(_1ad){
arr=arr.split("");
}
if(Array.filter){
var _1ae=Array.filter(arr,_1ab,_1ac);
}else{
if(!_1ac){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_1ac=dj_global;
}
var _1ae=[];
for(var i=0;i<arr.length;i++){
if(_1ab.call(_1ac,arr[i],i,arr)){
_1ae.push(arr[i]);
}
}
}
if(_1ad){
return _1ae.join("");
}else{
return _1ae;
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
dojo.lang.toArray=function(_1b3,_1b4){
var _1b5=[];
for(var i=_1b4||0;i<_1b3.length;i++){
_1b5.push(_1b3[i]);
}
return _1b5;
};
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_1b7,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _1b7.replace(/\%\{(\w+)\}/g,function(_1ba,key){
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
var _1bd=str.split(" ");
for(var i=0;i<_1bd.length;i++){
_1bd[i]=_1bd[i].charAt(0).toUpperCase()+_1bd[i].substring(1);
}
return _1bd.join(" ");
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
var _1c2=escape(str);
var _1c3,re=/%u([0-9A-F]{4})/i;
while((_1c3=_1c2.match(re))){
var num=Number("0x"+_1c3[1]);
var _1c5=escape("&#"+num+";");
ret+=_1c2.substring(0,_1c3.index)+_1c5;
_1c2=_1c2.substring(_1c3.index+_1c3[0].length);
}
ret+=_1c2.replace(/\+/g,"%2B");
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
dojo.string.escapeXml=function(str,_1ca){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_1ca){
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
}else{
return str.substring(0,len).replace(/\.+$/,"")+"...";
}
};
dojo.string.endsWith=function(str,end,_1d3){
if(_1d3){
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
dojo.string.startsWith=function(str,_1d7,_1d8){
if(_1d8){
str=str.toLowerCase();
_1d7=_1d7.toLowerCase();
}
return str.indexOf(_1d7)==0;
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
dojo.string.normalizeNewlines=function(text,_1de){
if(_1de=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_1de=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n");
text=text.replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_1e0){
var _1e1=[];
for(var i=0,prevcomma=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_1e0){
_1e1.push(str.substring(prevcomma,i));
prevcomma=i+1;
}
}
_1e1.push(str.substr(prevcomma));
return _1e1;
};
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_1e4,wrap,_1e6){
dojo.json.jsonRegistry.register(name,_1e4,wrap,_1e6);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _1e9=typeof (o);
if(_1e9=="undefined"){
return "undefined";
}else{
if((_1e9=="number")||(_1e9=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_1e9=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _1eb;
if(typeof (o.__json__)=="function"){
_1eb=o.__json__();
if(o!==_1eb){
return me(_1eb);
}
}
if(typeof (o.json)=="function"){
_1eb=o.json();
if(o!==_1eb){
return me(_1eb);
}
}
if(_1e9!="function"&&typeof (o.length)=="number"){
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
_1eb=dojo.json.jsonRegistry.match(o);
return me(_1eb);
}
catch(e){
}
if(_1e9=="function"){
return null;
}
res=[];
for(var k in o){
var _1f0;
if(typeof (k)=="number"){
_1f0="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_1f0=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_1f0+":"+val);
}
return "{"+res.join(",")+"}";
}};
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
var _1f2=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_1f2.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_1f4,_1f5){
var node=_1f4.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_1f5&&node&&node.tagName&&node.tagName.toLowerCase()!=_1f5.toLowerCase()){
node=dojo.dom.nextElement(node,_1f5);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_1f7,_1f8){
var node=_1f7.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_1f8&&node&&node.tagName&&node.tagName.toLowerCase()!=_1f8.toLowerCase()){
node=dojo.dom.prevElement(node,_1f8);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_1fb){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_1fb&&_1fb.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_1fb);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_1fd){
if(!node){
return null;
}
if(_1fd){
_1fd=_1fd.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_1fd&&_1fd.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_1fd);
}
return node;
};
dojo.dom.moveChildren=function(_1fe,_1ff,trim){
var _201=0;
if(trim){
while(_1fe.hasChildNodes()&&_1fe.firstChild.nodeType==dojo.dom.TEXT_NODE){
_1fe.removeChild(_1fe.firstChild);
}
while(_1fe.hasChildNodes()&&_1fe.lastChild.nodeType==dojo.dom.TEXT_NODE){
_1fe.removeChild(_1fe.lastChild);
}
}
while(_1fe.hasChildNodes()){
_1ff.appendChild(_1fe.firstChild);
_201++;
}
return _201;
};
dojo.dom.copyChildren=function(_202,_203,trim){
var _205=_202.cloneNode(true);
return this.moveChildren(_205,_203,trim);
};
dojo.dom.removeChildren=function(node){
var _207=node.childNodes.length;
while(node.hasChildNodes()){
node.removeChild(node.firstChild);
}
return _207;
};
dojo.dom.replaceChildren=function(node,_209){
dojo.dom.removeChildren(node);
node.appendChild(_209);
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_20c,_20d){
var _20e=[];
var _20f=(_20c&&(_20c instanceof Function||typeof _20c=="function"));
while(node){
if(!_20f||_20c(node)){
_20e.push(node);
}
if(_20d&&_20e.length>0){
return _20e[0];
}
node=node.parentNode;
}
if(_20d){
return null;
}
return _20e;
};
dojo.dom.getAncestorsByTag=function(node,tag,_212){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_212);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_217,_218){
if(_218&&node){
node=node.parentNode;
}
while(node){
if(node==_217){
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
var _21b=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _21c=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_21c.length;i++){
try{
doc=new ActiveXObject(_21c[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_21b.implementation)&&(_21b.implementation.createDocument)){
doc=_21b.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_21f){
if(!_21f){
_21f="text/xml";
}
if(!dj_undef("DOMParser")){
var _220=new DOMParser();
return _220.parseFromString(str,_21f);
}else{
if(!dj_undef("ActiveXObject")){
var _221=dojo.dom.createDocument();
if(_221){
_221.async=false;
_221.loadXML(str);
return _221;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _222=dojo.doc();
if(_222.createElement){
var tmp=_222.createElement("xml");
tmp.innerHTML=str;
if(_222.implementation&&_222.implementation.createDocument){
var _224=_222.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_224.importNode(tmp.childNodes.item(i),true);
}
return _224;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_227){
if(_227.firstChild){
_227.insertBefore(node,_227.firstChild);
}else{
_227.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_22a){
if(_22a!=true&&(node===ref||node.nextSibling===ref)){
return false;
}
var _22b=ref.parentNode;
_22b.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_22e){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_22e!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_22e);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_232){
if((!node)||(!ref)||(!_232)){
return false;
}
switch(_232.toLowerCase()){
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
dojo.dom.insertAtIndex=function(node,_234,_235){
var _236=_234.childNodes;
if(!_236.length){
_234.appendChild(node);
return true;
}
var _237=null;
for(var i=0;i<_236.length;i++){
var _239=_236.item(i)["getAttribute"]?parseInt(_236.item(i).getAttribute("dojoinsertionindex")):-1;
if(_239<_235){
_237=_236.item(i);
}
}
if(_237){
return dojo.dom.insertAfter(node,_237);
}else{
return dojo.dom.insertBefore(node,_236.item(0));
}
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _23c=dojo.doc();
dojo.dom.replaceChildren(node,_23c.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _23d="";
if(node==null){
return _23d;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_23d+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_23d+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _23d;
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
dojo.dom.setAttributeNS=function(elem,_243,_244,_245){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_243,_244,_245);
}else{
var _246=elem.ownerDocument;
var _247=_246.createNode(2,_244,_243);
_247.nodeValue=_245;
elem.setAttributeNode(_247);
}
};
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
dojo.undo.browser={initialHref:window.location.href,initialHash:window.location.hash,moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
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
var _24c=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_24e){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_24c.apply(this,[_24e]);
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
var _24f=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_251){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_24f){
_24f.apply(this,[_251]);
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
},iframeLoaded:function(evt,_254){
if(!dojo.render.html.opera){
var _255=this._getUrlQuery(_254.href);
if(_255==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_255==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_255==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _256=this.historyStack.pop();
if(!_256){
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
this.forwardStack.push(_256);
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
var _25d=url.split("?");
if(_25d.length<2){
return null;
}else{
return _25d[1];
}
},_loadIframeHistory:function(){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
dojo.io.checkChildrenForFile=function(node){
var _260=false;
var _261=node.getElementsByTagName("input");
dojo.lang.forEach(_261,function(_262){
if(_260){
return;
}
if(_262.getAttribute("type")=="file"){
_260=true;
}
});
return _260;
};
dojo.io.formHasFile=function(_263){
return dojo.io.checkChildrenForFile(_263);
};
dojo.io.updateNode=function(node,_265){
node=dojo.byId(node);
var args=_265;
if(dojo.lang.isString(_265)){
args={url:_265};
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
dojo.io.encodeForm=function(_26c,_26d,_26e){
if((!_26c)||(!_26c.tagName)||(!_26c.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_26e){
_26e=dojo.io.formFilter;
}
var enc=/utf/i.test(_26d||"")?encodeURIComponent:dojo.string.encodeAscii;
var _270=[];
for(var i=0;i<_26c.elements.length;i++){
var elm=_26c.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_26e(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_270.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_270.push(name+"="+enc(elm.value));
}
}else{
_270.push(name+"="+enc(elm.value));
}
}
}
var _276=_26c.getElementsByTagName("input");
for(var i=0;i<_276.length;i++){
var _277=_276[i];
if(_277.type.toLowerCase()=="image"&&_277.form==_26c&&_26e(_277)){
var name=enc(_277.name);
_270.push(name+"="+enc(_277.value));
_270.push(name+".x=0");
_270.push(name+".y=0");
}
}
return _270.join("&")+"&";
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
var _27d=form.getElementsByTagName("input");
for(var i=0;i<_27d.length;i++){
var _27e=_27d[i];
if(_27e.type.toLowerCase()=="image"&&_27e.form==form){
this.connect(_27e,"onclick","click");
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
var _285=false;
if(node.disabled||!node.name){
_285=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_285=node==this.clickedButton;
}else{
_285=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _285;
},connect:function(_286,_287,_288){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_286,_287,this,_288);
}else{
var fcn=dojo.lang.hitch(this,_288);
_286[_287]=function(e){
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
var _28b=this;
var _28c={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_28e,_28f){
return url+"|"+_28e+"|"+_28f.toLowerCase();
}
function addToCache(url,_291,_292,http){
_28c[getCacheKey(url,_291,_292)]=http;
}
function getFromCache(url,_295,_296){
return _28c[getCacheKey(url,_295,_296)];
}
this.clearCache=function(){
_28c={};
};
function doLoad(_297,http,url,_29a,_29b){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_297.method.toLowerCase()=="head"){
var _29d=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _29d;
};
var _29e=_29d.split(/[\r\n]+/g);
for(var i=0;i<_29e.length;i++){
var pair=_29e[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_297.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_297.mimetype=="text/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_297.mimetype=="application/xml")||(_297.mimetype=="text/xml")){
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
if(_29b){
addToCache(url,_29a,_297.method,http);
}
_297[(typeof _297.load=="function")?"load":"handle"]("load",ret,http,_297);
}else{
var _2a1=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_297[(typeof _297.error=="function")?"error":"handle"]("error",_2a1,http,_297);
}
}
function setHeaders(http,_2a3){
if(_2a3["headers"]){
for(var _2a4 in _2a3["headers"]){
if(_2a4.toLowerCase()=="content-type"&&!_2a3["contentType"]){
_2a3["contentType"]=_2a3["headers"][_2a4];
}else{
http.setRequestHeader(_2a4,_2a3["headers"][_2a4]);
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
if(!dojo.hostenv._blockAsync&&!_28b._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
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
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _2a8=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_2a9){
return _2a8&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json"],(_2a9["mimetype"].toLowerCase()||""))&&!(_2a9["formNode"]&&dojo.io.formHasFile(_2a9["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_2aa){
if(!_2aa["url"]){
if(!_2aa["formNode"]&&(_2aa["backButton"]||_2aa["back"]||_2aa["changeUrl"]||_2aa["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_2aa);
return true;
}
}
var url=_2aa.url;
var _2ac="";
if(_2aa["formNode"]){
var ta=_2aa.formNode.getAttribute("action");
if((ta)&&(!_2aa["url"])){
url=ta;
}
var tp=_2aa.formNode.getAttribute("method");
if((tp)&&(!_2aa["method"])){
_2aa.method=tp;
}
_2ac+=dojo.io.encodeForm(_2aa.formNode,_2aa.encoding,_2aa["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_2aa["file"]){
_2aa.method="post";
}
if(!_2aa["method"]){
_2aa.method="get";
}
if(_2aa.method.toLowerCase()=="get"){
_2aa.multipart=false;
}else{
if(_2aa["file"]){
_2aa.multipart=true;
}else{
if(!_2aa["multipart"]){
_2aa.multipart=false;
}
}
}
if(_2aa["backButton"]||_2aa["back"]||_2aa["changeUrl"]){
dojo.undo.browser.addToHistory(_2aa);
}
var _2af=_2aa["content"]||{};
if(_2aa.sendTransport){
_2af["dojo.transport"]="xmlhttp";
}
do{
if(_2aa.postContent){
_2ac=_2aa.postContent;
break;
}
if(_2af){
_2ac+=dojo.io.argsFromMap(_2af,_2aa.encoding);
}
if(_2aa.method.toLowerCase()=="get"||!_2aa.multipart){
break;
}
var t=[];
if(_2ac.length){
var q=_2ac.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_2aa.file){
if(dojo.lang.isArray(_2aa.file)){
for(var i=0;i<_2aa.file.length;++i){
var o=_2aa.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_2aa.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_2ac=t.join("\r\n");
}
}while(false);
var _2b5=_2aa["sync"]?false:true;
var _2b6=_2aa["preventCache"]||(this.preventCache==true&&_2aa["preventCache"]!=false);
var _2b7=_2aa["useCache"]==true||(this.useCache==true&&_2aa["useCache"]!=false);
if(!_2b6&&_2b7){
var _2b8=getFromCache(url,_2ac,_2aa.method);
if(_2b8){
doLoad(_2aa,_2b8,url,_2ac,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_2aa);
var _2ba=false;
if(_2b5){
var _2bb=this.inFlight.push({"req":_2aa,"http":http,"url":url,"query":_2ac,"useCache":_2b7,"startTime":_2aa.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_28b._blockAsync=true;
}
if(_2aa.method.toLowerCase()=="post"){
http.open("POST",url,_2b5);
setHeaders(http,_2aa);
http.setRequestHeader("Content-Type",_2aa.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_2aa.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_2ac);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_2aa,{status:404},url,_2ac,_2b7);
}
}else{
var _2bc=url;
if(_2ac!=""){
_2bc+=(_2bc.indexOf("?")>-1?"&":"?")+_2ac;
}
if(_2b6){
_2bc+=(dojo.string.endsWithAny(_2bc,"?","&")?"":(_2bc.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
http.open(_2aa.method.toUpperCase(),_2bc,_2b5);
setHeaders(http,_2aa);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_2aa,{status:404},url,_2ac,_2b7);
}
}
if(!_2b5){
doLoad(_2aa,http,url,_2ac,_2b7);
_28b._blockAsync=false;
}
_2aa.abort=function(){
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
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_2be,uri){
var loc=dojo.hostenv.getModulePrefix(_2be);
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri()+loc,uri);
};
this.nsUri=function(ns,uri){
dojo.deprecated("dojo.uri.nsUri","replaced by dojo.uri.moduleUri","0.4");
return dojo.uri.moduleUri(ns,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _2c5=new dojo.uri.Uri(arguments[i].toString());
var _2c6=new dojo.uri.Uri(uri.toString());
if(_2c5.path==""&&_2c5.scheme==null&&_2c5.authority==null&&_2c5.query==null){
if(_2c5.fragment!=null){
_2c6.fragment=_2c5.fragment;
}
_2c5=_2c6;
}else{
if(_2c5.scheme==null){
_2c5.scheme=_2c6.scheme;
if(_2c5.authority==null){
_2c5.authority=_2c6.authority;
if(_2c5.path.charAt(0)!="/"){
var path=_2c6.path.substring(0,_2c6.path.lastIndexOf("/")+1)+_2c5.path;
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
_2c5.path=segs.join("/");
}
}
}
}
uri="";
if(_2c5.scheme!=null){
uri+=_2c5.scheme+":";
}
if(_2c5.authority!=null){
uri+="//"+_2c5.authority;
}
uri+=_2c5.path;
if(_2c5.query!=null){
uri+="?"+_2c5.query;
}
if(_2c5.fragment!=null){
uri+="#"+_2c5.fragment;
}
}
this.uri=uri.toString();
var _2ca="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_2ca));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_2ca="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_2ca));
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
dojo.provide("dojo.uri.*");
dojo.provide("dojo.io.IframeIO");
dojo.io.createIFrame=function(_2cc,_2cd,uri){
if(window[_2cc]){
return window[_2cc];
}
if(window.frames[_2cc]){
return window.frames[_2cc];
}
var r=dojo.render.html;
var _2d0=null;
var turi=uri||dojo.uri.dojoUri("iframe_history.html?noInit=true");
var _2d2=((r.ie)&&(dojo.render.os.win))?"<iframe name=\""+_2cc+"\" src=\""+turi+"\" onload=\""+_2cd+"\">":"iframe";
_2d0=document.createElement(_2d2);
with(_2d0){
name=_2cc;
setAttribute("name",_2cc);
id=_2cc;
}
dojo.body().appendChild(_2d0);
window[_2cc]=_2d0;
with(_2d0.style){
if(!r.safari){
position="absolute";
}
left=top="0px";
height=width="1px";
visibility="hidden";
}
if(!r.ie){
dojo.io.setIFrameSrc(_2d0,turi,true);
_2d0.onload=new Function(_2cd);
}
return _2d0;
};
dojo.io.IframeTransport=new function(){
var _2d3=this;
this.currentRequest=null;
this.requestQueue=[];
this.iframeName="dojoIoIframe";
this.fireNextRequest=function(){
if((this.currentRequest)||(this.requestQueue.length==0)){
return;
}
var cr=this.currentRequest=this.requestQueue.shift();
cr._contentToClean=[];
var fn=cr["formNode"];
var _2d6=cr["content"]||{};
if(cr.sendTransport){
_2d6["dojo.transport"]="iframe";
}
if(fn){
if(_2d6){
for(var x in _2d6){
if(!fn[x]){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_2d6[x]+"'>");
fn.appendChild(tn);
}else{
tn=document.createElement("input");
fn.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_2d6[x];
}
cr._contentToClean.push(x);
}else{
fn[x].value=_2d6[x];
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
var _2d9=dojo.io.argsFromMap(this.currentRequest.content);
var _2da=(cr.url.indexOf("?")>-1?"&":"?")+_2d9;
dojo.io.setIFrameSrc(this.iframe,_2da,true);
}
};
this.canHandle=function(_2db){
return ((dojo.lang.inArray(["text/plain","text/html","text/javascript","text/json"],_2db["mimetype"]))&&((_2db["formNode"])&&(dojo.io.checkChildrenForFile(_2db["formNode"])))&&(dojo.lang.inArray(["post","get"],_2db["method"].toLowerCase()))&&(!((_2db["sync"])&&(_2db["sync"]==true))));
};
this.bind=function(_2dc){
if(!this["iframe"]){
this.setUpIframe();
}
this.requestQueue.push(_2dc);
this.fireNextRequest();
return;
};
this.setUpIframe=function(){
this.iframe=dojo.io.createIFrame(this.iframeName,"dojo.io.IframeTransport.iframeOnload();");
};
this.iframeOnload=function(){
if(!_2d3.currentRequest){
_2d3.fireNextRequest();
return;
}
var req=_2d3.currentRequest;
var _2de=req._contentToClean;
for(var i=0;i<_2de.length;i++){
var key=_2de[i];
if(dojo.render.html.safari){
var _2e1=req.formNode;
for(var j=0;j<_2e1.childNodes.length;j++){
var _2e3=_2e1.childNodes[j];
if(_2e3.name==key){
var _2e4=_2e3.parentNode;
_2e4.removeChild(_2e3);
break;
}
}
}else{
if(req.formNode){
var _2e5=req.formNode[key];
req.formNode.removeChild(_2e5);
req.formNode[key]=null;
}
}
}
if(req["_originalAction"]){
req.formNode.setAttribute("action",req._originalAction);
}
req.formNode.setAttribute("target",req._originalTarget);
req.formNode.target=req._originalTarget;
var _2e6=function(_2e7){
var doc=_2e7.contentDocument||((_2e7.contentWindow)&&(_2e7.contentWindow.document))||((_2e7.name)&&(document.frames[_2e7.name])&&(document.frames[_2e7.name].document))||null;
return doc;
};
var ifd=_2e6(_2d3.iframe);
var _2ea;
var _2eb=false;
try{
var cmt=req.mimetype;
if((cmt=="text/javascript")||(cmt=="text/json")){
var js=ifd.getElementsByTagName("textarea")[0].value;
if(cmt=="text/json"){
js="("+js+")";
}
_2ea=dj_eval(js);
}else{
if(cmt=="text/html"){
_2ea=ifd;
}else{
_2ea=ifd.getElementsByTagName("textarea")[0].value;
}
}
_2eb=true;
}
catch(e){
var _2ee=new dojo.io.Error("IframeTransport Error");
if(dojo.lang.isFunction(req["error"])){
req.error("error",_2ee,req);
}
}
try{
if(_2eb&&dojo.lang.isFunction(req["load"])){
req.load("load",_2ea,req);
}
}
catch(e){
throw e;
}
finally{
_2d3.currentRequest=null;
_2d3.fireNextRequest();
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
var _2ef=0;
var _2f0=0;
for(var _2f1 in this._state){
_2ef++;
var _2f2=this._state[_2f1];
if(_2f2.isDone){
_2f0++;
delete this._state[_2f1];
}else{
if(!_2f2.isFinishing){
var _2f3=_2f2.kwArgs;
try{
if(_2f2.checkString&&eval("typeof("+_2f2.checkString+") != 'undefined'")){
_2f2.isFinishing=true;
this._finish(_2f2,"load");
_2f0++;
delete this._state[_2f1];
}else{
if(_2f3.timeoutSeconds&&_2f3.timeout){
if(_2f2.startTime+(_2f3.timeoutSeconds*1000)<(new Date()).getTime()){
_2f2.isFinishing=true;
this._finish(_2f2,"timeout");
_2f0++;
delete this._state[_2f1];
}
}else{
if(!_2f3.timeoutSeconds){
_2f0++;
}
}
}
}
catch(e){
_2f2.isFinishing=true;
this._finish(_2f2,"error",{status:this.DsrStatusCodes.Error,response:e});
}
}
}
}
if(_2f0>=_2ef){
clearInterval(this.inFlightTimer);
this.inFlightTimer=null;
}
};
this.canHandle=function(_2f4){
return dojo.lang.inArray(["text/javascript","text/json"],(_2f4["mimetype"].toLowerCase()))&&(_2f4["method"].toLowerCase()=="get")&&!(_2f4["formNode"]&&dojo.io.formHasFile(_2f4["formNode"]))&&(!_2f4["sync"]||_2f4["sync"]==false)&&!_2f4["file"]&&!_2f4["multipart"];
};
this.removeScripts=function(){
var _2f5=document.getElementsByTagName("script");
for(var i=0;_2f5&&i<_2f5.length;i++){
var _2f7=_2f5[i];
if(_2f7.className=="ScriptSrcTransport"){
var _2f8=_2f7.parentNode;
_2f8.removeChild(_2f7);
i--;
}
}
};
this.bind=function(_2f9){
var url=_2f9.url;
var _2fb="";
if(_2f9["formNode"]){
var ta=_2f9.formNode.getAttribute("action");
if((ta)&&(!_2f9["url"])){
url=ta;
}
var tp=_2f9.formNode.getAttribute("method");
if((tp)&&(!_2f9["method"])){
_2f9.method=tp;
}
_2fb+=dojo.io.encodeForm(_2f9.formNode,_2f9.encoding,_2f9["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
var _2fe=url.split("?");
if(_2fe&&_2fe.length==2){
url=_2fe[0];
_2fb+=(_2fb?"&":"")+_2fe[1];
}
if(_2f9["backButton"]||_2f9["back"]||_2f9["changeUrl"]){
dojo.undo.browser.addToHistory(_2f9);
}
var id=_2f9["apiId"]?_2f9["apiId"]:"id"+this._counter++;
var _300=_2f9["content"];
var _301=_2f9.jsonParamName;
if(_2f9.sendTransport||_301){
if(!_300){
_300={};
}
if(_2f9.sendTransport){
_300["dojo.transport"]="scriptsrc";
}
if(_301){
_300[_301]="dojo.io.ScriptSrcTransport._state."+id+".jsonpCall";
}
}
if(_2f9.postContent){
_2fb=_2f9.postContent;
}else{
if(_300){
_2fb+=((_2fb)?"&":"")+dojo.io.argsFromMap(_300,_2f9.encoding,_301);
}
}
if(_2f9["apiId"]){
_2f9["useRequestId"]=true;
}
var _302={"id":id,"idParam":"_dsrid="+id,"url":url,"query":_2fb,"kwArgs":_2f9,"startTime":(new Date()).getTime(),"isFinishing":false};
if(!url){
this._finish(_302,"error",{status:this.DsrStatusCodes.Error,statusText:"url.none"});
return;
}
if(_300&&_300[_301]){
_302.jsonp=_300[_301];
_302.jsonpCall=function(data){
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
if(_2f9["useRequestId"]||_2f9["checkString"]||_302["jsonp"]){
this._state[id]=_302;
}
if(_2f9["checkString"]){
_302.checkString=_2f9["checkString"];
}
_302.constantParams=(_2f9["constantParams"]==null?"":_2f9["constantParams"]);
if(_2f9["preventCache"]||(this.preventCache==true&&_2f9["preventCache"]!=false)){
_302.nocacheParam="dojo.preventCache="+new Date().valueOf();
}else{
_302.nocacheParam="";
}
var _304=_302.url.length+_302.query.length+_302.constantParams.length+_302.nocacheParam.length+this._extraPaddingLength;
if(_2f9["useRequestId"]){
_304+=_302.idParam.length;
}
if(!_2f9["checkString"]&&_2f9["useRequestId"]&&!_302["jsonp"]&&!_2f9["forceSingleRequest"]&&_304>this.maxUrlLength){
if(url>this.maxUrlLength){
this._finish(_302,"error",{status:this.DsrStatusCodes.Error,statusText:"url.tooBig"});
return;
}else{
this._multiAttach(_302,1);
}
}else{
var _305=[_302.constantParams,_302.nocacheParam,_302.query];
if(_2f9["useRequestId"]&&!_302["jsonp"]){
_305.unshift(_302.idParam);
}
var _306=this._buildUrl(_302.url,_305);
_302.finalUrl=_306;
this._attach(_302.id,_306);
}
this.startWatchingInFlight();
};
this._counter=1;
this._state={};
this._extraPaddingLength=16;
this._buildUrl=function(url,_308){
var _309=url;
var _30a="?";
for(var i=0;i<_308.length;i++){
if(_308[i]){
_309+=_30a+_308[i];
_30a="&";
}
}
return _309;
};
this._attach=function(id,url){
var _30e=document.createElement("script");
_30e.type="text/javascript";
_30e.src=url;
_30e.id=id;
_30e.className="ScriptSrcTransport";
document.getElementsByTagName("head")[0].appendChild(_30e);
};
this._multiAttach=function(_30f,part){
if(_30f.query==null){
this._finish(_30f,"error",{status:this.DsrStatusCodes.Error,statusText:"query.null"});
return;
}
if(!_30f.constantParams){
_30f.constantParams="";
}
var _311=this.maxUrlLength-_30f.idParam.length-_30f.constantParams.length-_30f.url.length-_30f.nocacheParam.length-this._extraPaddingLength;
var _312=_30f.query.length<_311;
var _313;
if(_312){
_313=_30f.query;
_30f.query=null;
}else{
var _314=_30f.query.lastIndexOf("&",_311-1);
var _315=_30f.query.lastIndexOf("=",_311-1);
if(_314>_315||_315==_311-1){
_313=_30f.query.substring(0,_314);
_30f.query=_30f.query.substring(_314+1,_30f.query.length);
}else{
_313=_30f.query.substring(0,_311);
var _316=_313.substring((_314==-1?0:_314+1),_315);
_30f.query=_316+"="+_30f.query.substring(_311,_30f.query.length);
}
}
var _317=[_313,_30f.idParam,_30f.constantParams,_30f.nocacheParam];
if(!_312){
_317.push("_part="+part);
}
var url=this._buildUrl(_30f.url,_317);
this._attach(_30f.id+"_"+part,url);
};
this._finish=function(_319,_31a,_31b){
if(_31a!="partOk"&&!_319.kwArgs[_31a]&&!_319.kwArgs["handle"]){
if(_31a=="error"){
_319.isDone=true;
throw _31b;
}
}else{
switch(_31a){
case "load":
var _31c=_31b?_31b.response:null;
if(!_31c){
_31c=_31b;
}
_319.kwArgs[(typeof _319.kwArgs.load=="function")?"load":"handle"]("load",_31c,_31b,_319.kwArgs);
_319.isDone=true;
break;
case "partOk":
var part=parseInt(_31b.response.part,10)+1;
if(_31b.response.constantParams){
_319.constantParams=_31b.response.constantParams;
}
this._multiAttach(_319,part);
_319.isDone=false;
break;
case "error":
_319.kwArgs[(typeof _319.kwArgs.error=="function")?"error":"handle"]("error",_31b.response,_31b,_319.kwArgs);
_319.isDone=true;
break;
default:
_319.kwArgs[(typeof _319.kwArgs[_31a]=="function")?_31a:"handle"](_31a,_31b,_31b,_319.kwArgs);
_319.isDone=true;
}
}
};
dojo.io.transports.addTransport("ScriptSrcTransport");
};
window.onscriptload=function(_31e){
var _31f=null;
var _320=dojo.io.ScriptSrcTransport;
if(_320._state[_31e.id]){
_31f=_320._state[_31e.id];
}else{
var _321;
for(var _322 in _320._state){
_321=_320._state[_322];
if(_321.finalUrl&&_321.finalUrl==_31e.id){
_31f=_321;
break;
}
}
if(_31f==null){
var _323=document.getElementsByTagName("script");
for(var i=0;_323&&i<_323.length;i++){
var _325=_323[i];
if(_325.getAttribute("class")=="ScriptSrcTransport"&&_325.src==_31e.id){
_31f=_320._state[_325.id];
break;
}
}
}
if(_31f==null){
throw "No matching state for onscriptload event.id: "+_31e.id;
}
}
var _326="error";
switch(_31e.status){
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Continue:
_326="partOk";
break;
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Ok:
_326="load";
break;
}
_320._finish(_31f,_326,_31e);
};
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_328,days,path,_32b,_32c){
var _32d=-1;
if(typeof days=="number"&&days>=0){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_32d=d.toGMTString();
}
_328=escape(_328);
document.cookie=name+"="+_328+";"+(_32d!=-1?" expires="+_32d+";":"")+(path?"path="+path:"")+(_32b?"; domain="+_32b:"")+(_32c?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _331=document.cookie.substring(idx+name.length+1);
var end=_331.indexOf(";");
if(end==-1){
end=_331.length;
}
_331=_331.substring(0,end);
_331=unescape(_331);
return _331;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_338,_339,_33a){
if(arguments.length==5){
_33a=_338;
_338=null;
_339=null;
}
var _33b=[],cookie,value="";
if(!_33a){
cookie=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!cookie){
cookie={};
}
for(var prop in obj){
if(prop==null){
delete cookie[prop];
}else{
if(typeof obj[prop]=="string"||typeof obj[prop]=="number"){
cookie[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in cookie){
_33b.push(escape(prop)+"="+escape(cookie[prop]));
}
value=_33b.join("&");
}
dojo.io.cookie.setCookie(name,value,days,path,_338,_339);
};
dojo.io.cookie.getObjectCookie=function(name){
var _33e=null,cookie=dojo.io.cookie.getCookie(name);
if(cookie){
_33e={};
var _33f=cookie.split("&");
for(var i=0;i<_33f.length;i++){
var pair=_33f[i].split("=");
var _342=pair[1];
if(isNaN(_342)){
_342=unescape(pair[1]);
}
_33e[unescape(pair[0])]=_342;
}
}
return _33e;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _343=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_343=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.provide("dojo.event.common");
dojo.event=new function(){
this.canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_345){
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
var _348=dl.nameAnonFunc(args[2],ao.adviceObj,_345);
ao.adviceFunc=_348;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _348=dl.nameAnonFunc(args[0],ao.srcObj,_345);
ao.srcFunc=_348;
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
var _348=dl.nameAnonFunc(args[1],dj_global,_345);
ao.srcFunc=_348;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _348=dl.nameAnonFunc(args[3],dj_global,_345);
ao.adviceObj=dj_global;
ao.adviceFunc=_348;
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
var _348=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_345);
ao.aroundFunc=_348;
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
var _34a={};
for(var x in ao){
_34a[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_34a.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_34a));
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
var _352;
if((arguments.length==1)&&(typeof a1=="object")){
_352=a1;
}else{
_352={srcObj:a1,srcFunc:a2};
}
_352.adviceFunc=function(){
var _353=[];
for(var x=0;x<arguments.length;x++){
_353.push(arguments[x]);
}
dojo.debug("("+_352.srcObj+")."+_352.srcFunc,":",_353.join(", "));
};
this.kwConnect(_352);
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
this._kwConnectImpl=function(_35a,_35b){
var fn=(_35b)?"disconnect":"connect";
if(typeof _35a["srcFunc"]=="function"){
_35a.srcObj=_35a["srcObj"]||dj_global;
var _35d=dojo.lang.nameAnonFunc(_35a.srcFunc,_35a.srcObj,true);
_35a.srcFunc=_35d;
}
if(typeof _35a["adviceFunc"]=="function"){
_35a.adviceObj=_35a["adviceObj"]||dj_global;
var _35d=dojo.lang.nameAnonFunc(_35a.adviceFunc,_35a.adviceObj,true);
_35a.adviceFunc=_35d;
}
return dojo.event[fn]((_35a["type"]||_35a["adviceType"]||"after"),_35a["srcObj"]||dj_global,_35a["srcFunc"],_35a["adviceObj"]||_35a["targetObj"]||dj_global,_35a["adviceFunc"]||_35a["targetFunc"],_35a["aroundObj"],_35a["aroundFunc"],_35a["once"],_35a["delay"],_35a["rate"],_35a["adviceMsg"]||false);
};
this.kwConnect=function(_35e){
return this._kwConnectImpl(_35e,false);
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
this.kwDisconnect=function(_361){
return this._kwConnectImpl(_361,true);
};
};
dojo.event.MethodInvocation=function(_362,obj,args){
this.jp_=_362;
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
dojo.event.MethodJoinPoint=function(obj,_36a){
this.object=obj||dj_global;
this.methodname=_36a;
this.methodfunc=this.object[_36a];
this.before=[];
this.after=[];
this.around=[];
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_36c){
if(!obj){
obj=dj_global;
}
if(!obj[_36c]){
obj[_36c]=function(){
};
if(!obj[_36c]){
dojo.raise("Cannot set do-nothing method on that object "+_36c);
}
}else{
if((!dojo.lang.isFunction(obj[_36c]))&&(!dojo.lang.isAlien(obj[_36c]))){
return null;
}
}
var _36d=_36c+"$joinpoint";
var _36e=_36c+"$joinpoint$method";
var _36f=obj[_36d];
if(!_36f){
var _370=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_370=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_36d,_36e,_36c]);
}
}
var _371=obj[_36c].length;
obj[_36e]=obj[_36c];
_36f=obj[_36d]=new dojo.event.MethodJoinPoint(obj,_36e);
obj[_36c]=function(){
var args=[];
if((_370)&&(!arguments.length)){
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
if((x==0)&&(_370)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _36f.run.apply(_36f,args);
};
obj[_36c].__preJoinArity=_371;
}
return _36f;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _377=[];
for(var x=0;x<args.length;x++){
_377[x]=args[x];
}
var _379=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _37b=marr[0]||dj_global;
var _37c=marr[1];
if(!_37b[_37c]){
dojo.raise("function \""+_37c+"\" does not exist on \""+_37b+"\"");
}
var _37d=marr[2]||dj_global;
var _37e=marr[3];
var msg=marr[6];
var _380;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _37b[_37c].apply(_37b,to.args);
}};
to.args=_377;
var _382=parseInt(marr[4]);
var _383=((!isNaN(_382))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _386=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event.canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_379(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_37e){
_37d[_37e].call(_37d,to);
}else{
if((_383)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_37b[_37c].call(_37b,to);
}else{
_37b[_37c].apply(_37b,args);
}
},_382);
}else{
if(msg){
_37b[_37c].call(_37b,to);
}else{
_37b[_37c].apply(_37b,args);
}
}
}
};
if(this.before.length>0){
dojo.lang.forEach(this.before.concat(new Array()),_379);
}
var _389;
if(this.around.length>0){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_389=mi.proceed();
}else{
if(this.methodfunc){
_389=this.object[this.methodname].apply(this.object,args);
}
}
if(this.after.length>0){
dojo.lang.forEach(this.after.concat(new Array()),_379);
}
return (this.methodfunc)?_389:null;
},getArr:function(kind){
var arr=this.after;
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
arr=this.before;
}else{
if(kind=="around"){
arr=this.around;
}
}
return arr;
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"]);
},addAdvice:function(_38e,_38f,_390,_391,_392,_393,once,_395,rate,_397){
var arr=this.getArr(_392);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_38e,_38f,_390,_391,_395,rate,_397];
if(once){
if(this.hasAdvice(_38e,_38f,_392,arr)>=0){
return;
}
}
if(_393=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_39a,_39b,_39c,arr){
if(!arr){
arr=this.getArr(_39c);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _39b=="object")?(new String(_39b)).toString():_39b;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_39a)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_3a2,_3a3,_3a4,once){
var arr=this.getArr(_3a4);
var ind=this.hasAdvice(_3a2,_3a3,_3a4,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_3a2,_3a3,_3a4,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_3a8){
if(!this.topics[_3a8]){
this.topics[_3a8]=new this.TopicImpl(_3a8);
}
return this.topics[_3a8];
};
this.registerPublisher=function(_3a9,obj,_3ab){
var _3a9=this.getTopic(_3a9);
_3a9.registerPublisher(obj,_3ab);
};
this.subscribe=function(_3ac,obj,_3ae){
var _3ac=this.getTopic(_3ac);
_3ac.subscribe(obj,_3ae);
};
this.unsubscribe=function(_3af,obj,_3b1){
var _3af=this.getTopic(_3af);
_3af.unsubscribe(obj,_3b1);
};
this.destroy=function(_3b2){
this.getTopic(_3b2).destroy();
delete this.topics[_3b2];
};
this.publishApply=function(_3b3,args){
var _3b3=this.getTopic(_3b3);
_3b3.sendMessage.apply(_3b3,args);
};
this.publish=function(_3b5,_3b6){
var _3b5=this.getTopic(_3b5);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_3b5.sendMessage.apply(_3b5,args);
};
};
dojo.event.topic.TopicImpl=function(_3b9){
this.topicName=_3b9;
this.subscribe=function(_3ba,_3bb){
var tf=_3bb||_3ba;
var to=(!_3bb)?dj_global:_3ba;
dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_3be,_3bf){
var tf=(!_3bf)?_3be:_3bf;
var to=(!_3bf)?null:_3be;
dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.destroy=function(){
dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage").disconnect();
};
this.registerPublisher=function(_3c2,_3c3){
dojo.event.connect(_3c2,_3c3,this,"sendMessage");
};
this.sendMessage=function(_3c4){
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
this.clobber=function(_3c7){
var na;
var tna;
if(_3c7){
tna=_3c7.all||_3c7.getElementsByTagName("*");
na=[_3c7];
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
var _3cb={};
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
var _3cf=0;
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
this.addClobberNodeAttrs=function(node,_3d3){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_3d3.length;x++){
node.__clobberAttrs__.push(_3d3[x]);
}
};
this.removeListener=function(node,_3d6,fp,_3d8){
if(!_3d8){
var _3d8=false;
}
_3d6=_3d6.toLowerCase();
if((_3d6=="onkey")||(_3d6=="key")){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_3d8);
}
_3d6="onkeypress";
}
if(_3d6.substr(0,2)=="on"){
_3d6=_3d6.substr(2);
}
if(node.removeEventListener){
node.removeEventListener(_3d6,fp,_3d8);
}
};
this.addListener=function(node,_3da,fp,_3dc,_3dd){
if(!node){
return;
}
if(!_3dc){
var _3dc=false;
}
_3da=_3da.toLowerCase();
if((_3da=="onkey")||(_3da=="key")){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_3dc,_3dd);
}
_3da="onkeypress";
}
if(_3da.substr(0,2)!="on"){
_3da="on"+_3da;
}
if(!_3dd){
var _3de=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_3dc){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_3de=fp;
}
if(node.addEventListener){
node.addEventListener(_3da.substr(2),_3de,_3dc);
return _3de;
}else{
if(typeof node[_3da]=="function"){
var _3e1=node[_3da];
node[_3da]=function(e){
_3e1(e);
return _3de(e);
};
}else{
node[_3da]=_3de;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_3da]);
}
return _3de;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_3e4,_3e5){
if(typeof _3e4!="function"){
dojo.raise("listener not a function: "+_3e4);
}
dojo.event.browser.currentEvent.currentTarget=_3e5;
return _3e4.call(_3e5,dojo.event.browser.currentEvent);
};
this.stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this.preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_3e8){
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
var _3ea=evt.keyCode;
if(_3ea>=65&&_3ea<=90&&evt.shiftKey==false){
_3ea+=32;
}
if(_3ea>=1&&_3ea<=26&&evt.ctrlKey){
_3ea+=96;
}
evt.key=String.fromCharCode(_3ea);
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
var _3ea=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_3ea+=32;
}
evt.key=String.fromCharCode(_3ea);
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
evt.currentTarget=(_3e8?_3e8:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _3ec=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_3ec.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_3ec.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this.stopPropagation;
evt.preventDefault=this.preventDefault;
}
return evt;
};
this.stopEvent=function(ev){
if(window.event){
ev.returnValue=false;
ev.cancelBubble=true;
}else{
ev.preventDefault();
ev.stopPropagation();
}
};
};
dojo.provide("dojo.event.*");
dojo.provide("dojo.io.cometd");
dojo.provide("cometd");
cometd=new function(){
this.initialized=false;
this.connected=false;
this.connectionTypes=new dojo.AdapterRegistry(true);
this.version=0.1;
this.minimumVersion=0.1;
this.clientId=null;
this.isXD=false;
this.handshakeReturn=null;
this.currentTransport=null;
this.url=null;
this.lastMessage=null;
this.globalTopicChannels={};
this.backlog=[];
this.tunnelInit=function(_3ee,_3ef){
};
this.tunnelCollapse=function(){
dojo.debug("tunnel collapsed!");
};
this.init=function(_3f0,root,_3f2){
_3f0=_3f0||{};
_3f0.version=this.version;
_3f0.minimumVersion=this.minimumVersion;
_3f0.channel="/meta/handshake";
this.url=root||djConfig["cometdRoot"];
if(!this.url){
dojo.debug("no cometd root specified in djConfig and no root passed");
return;
}
var _3f3={url:this.url,method:"POST",mimetype:"text/json",load:dojo.lang.hitch(this,"finishInit"),content:{"message":dojo.json.serialize([_3f0])}};
var _3f4="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=(""+window.location).match(new RegExp(_3f4));
if(r[4]){
var tmp=r[4].split(":");
var _3f7=tmp[0];
var _3f8=tmp[1]||"80";
r=this.url.match(new RegExp(_3f4));
if(r[4]){
tmp=r[4].split(":");
var _3f9=tmp[0];
var _3fa=tmp[1]||"80";
if((_3f9!=_3f7)||(_3fa!=_3f8)){
dojo.debug(_3f7,_3f9);
dojo.debug(_3f8,_3fa);
this.isXD=true;
_3f3.transport="ScriptSrcTransport";
_3f3.jsonParamName="jsonp";
}
}
}
if(_3f2){
dojo.lang.mixin(_3f3,_3f2);
}
return dojo.io.bind(_3f3);
};
this.finishInit=function(type,data,evt,_3fe){
this.handshakeReturn=data;
if(data["authSuccessful"]==false){
dojo.debug("cometd authentication failed");
return;
}
if(data.version<this.minimumVersion){
dojo.debug("cometd protocol version mismatch. We wanted",this.minimumVersion,"but got",data.version);
return;
}
this.currentTransport=this.connectionTypes.match(data.supportedConnectionTypes,data.version,this.isXD);
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
this.getRandStr=function(){
return Math.random().toString().substring(2,10);
};
this.deliver=function(_401){
dojo.lang.forEach(_401,this._deliver,this);
};
this._deliver=function(_402){
if(!_402["channel"]){
dojo.debug("cometd error: no channel for message!");
return;
}
if(!this.currentTransport){
this.backlog.push(["deliver",_402]);
return;
}
this.lastMessage=_402;
if((_402.channel.length>5)&&(_402.channel.substr(0,5)=="/meta")){
switch(_402.channel){
case "/meta/subscribe":
if(!_402.successful){
dojo.debug("cometd subscription error for channel",_402.channel,":",_402.error);
return;
}
this.subscribed(_402.subscription,_402);
break;
case "/meta/unsubscribe":
if(!_402.successful){
dojo.debug("cometd unsubscription error for channel",_402.channel,":",_402.error);
return;
}
this.unsubscribed(_402.subscription,_402);
break;
}
}
this.currentTransport.deliver(_402);
var _403=(this.globalTopicChannels[_402.channel])?_402.channel:"/cometd"+_402.channel;
dojo.event.topic.publish(_403,_402);
};
this.disconnect=function(){
if(!this.currentTransport){
dojo.debug("no current transport to disconnect from");
return;
}
this.currentTransport.disconnect();
};
this.publish=function(_404,data,_406){
if(!this.currentTransport){
this.backlog.push(["publish",_404,data,_406]);
return;
}
var _407={data:data,channel:_404};
if(_406){
dojo.lang.mixin(_407,_406);
}
return this.currentTransport.sendMessage(_407);
};
this.subscribe=function(_408,_409,_40a,_40b){
if(!this.currentTransport){
this.backlog.push(["subscribe",_408,_409,_40a,_40b]);
return;
}
if(_40a){
var _40c=(_409)?_408:"/cometd"+_408;
if(_409){
this.globalTopicChannels[_408]=true;
}
dojo.event.topic.subscribe(_40c,_40a,_40b);
}
return this.currentTransport.sendMessage({channel:"/meta/subscribe",subscription:_408});
};
this.subscribed=function(_40d,_40e){
dojo.debug(_40d);
dojo.debugShallow(_40e);
};
this.unsubscribe=function(_40f,_410,_411,_412){
if(!this.currentTransport){
this.backlog.push(["unsubscribe",_40f,_410,_411,_412]);
return;
}
if(_411){
var _413=(_410)?_40f:"/cometd"+_40f;
dojo.event.topic.unsubscribe(_413,_411,_412);
}
return this.currentTransport.sendMessage({channel:"/meta/unsubscribe",subscription:_40f});
};
this.unsubscribed=function(_414,_415){
dojo.debug(_414);
dojo.debugShallow(_415);
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
this.check=function(_416,_417,_418){
return ((!_418)&&(!dojo.render.html.safari)&&(dojo.lang.inArray(_416,"iframe")));
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
this.deliver=function(_419){
if(_419["timestamp"]){
this.lastTimestamp=_419.timestamp;
}
if(_419["id"]){
this.lastId=_419.id;
}
if((_419.channel.length>5)&&(_419.channel.substr(0,5)=="/meta")){
switch(_419.channel){
case "/meta/connect":
if(!_419.successful){
dojo.debug("cometd connection error:",_419.error);
return;
}
this.connectionId=_419.connectionId;
this.connected=true;
this.processBacklog();
break;
case "/meta/reconnect":
if(!_419.successful){
dojo.debug("cometd reconnection error:",_419.error);
return;
}
this.connected=true;
break;
case "/meta/subscribe":
if(!_419.successful){
dojo.debug("cometd subscription error for channel",_419.channel,":",_419.error);
return;
}
dojo.debug(_419.channel);
break;
}
}
};
this.widenDomain=function(_41a){
var cd=_41a||document.domain;
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
this.postToIframe=function(_41d,url){
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
for(var x in _41d){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_41d[x]+"'>");
this.phonyForm.appendChild(tn);
}else{
tn=document.createElement("input");
this.phonyForm.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_41d[x];
}
}
this.phonyForm.submit();
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_421,_422){
if((_422)||(this.connected)){
_421.connectionId=this.connectionId;
_421.clientId=cometd.clientId;
var _423={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_421])}};
return dojo.io.bind(_423);
}else{
this.backlog.push(_421);
}
};
this.startup=function(_424){
dojo.debug("startup!");
dojo.debug(dojo.json.serialize(_424));
if(this.connected){
return;
}
this.rcvNodeName="cometdRcv_"+cometd.getRandStr();
var _425=cometd.url+"/?tunnelInit=iframe";
if(false&&dojo.render.html.ie){
this.rcvNode=new ActiveXObject("htmlfile");
this.rcvNode.open();
this.rcvNode.write("<html>");
this.rcvNode.write("<script>document.domain = '"+document.domain+"'");
this.rcvNode.write("</html>");
this.rcvNode.close();
var _426=this.rcvNode.createElement("div");
this.rcvNode.appendChild(_426);
this.rcvNode.parentWindow.dojo=dojo;
_426.innerHTML="<iframe src='"+_425+"'></iframe>";
}else{
this.rcvNode=dojo.io.createIFrame(this.rcvNodeName,"",_425);
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
this.check=function(_427,_428,_429){
return ((!_429)&&(dojo.render.html.mozilla)&&(dojo.lang.inArray(_427,"mime-message-block")));
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
this.openTunnelWith=function(_42b,url){
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
dojo.debug(dojo.json.serialize(_42b));
this.xhr.send(dojo.io.argsFromMap(_42b,"utf8"));
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_42d,_42e){
if((_42e)||(this.connected)){
_42d.connectionId=this.connectionId;
_42d.clientId=cometd.clientId;
var _42f={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_42d])}};
return dojo.io.bind(_42f);
}else{
this.backlog.push(_42d);
}
};
this.startup=function(_430){
dojo.debugShallow(_430);
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
this.check=function(_431,_432,_433){
return ((!_433)&&(dojo.lang.inArray(_431,"long-polling")));
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
this.openTunnelWith=function(_434,url){
dojo.io.bind({url:(url||cometd.url),method:"post",content:_434,mimetype:"text/json",load:dojo.lang.hitch(this,function(type,data,evt,args){
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
this.sendMessage=function(_43a,_43b){
if((_43b)||(this.connected)){
_43a.connectionId=this.connectionId;
_43a.clientId=cometd.clientId;
var _43c={url:cometd.url||djConfig["cometdRoot"],method:"post",mimetype:"text/json",content:{message:dojo.json.serialize([_43a])}};
return dojo.io.bind(_43c);
}else{
this.backlog.push(_43a);
}
};
this.startup=function(_43d){
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
this.check=function(_43e,_43f,_440){
return dojo.lang.inArray(_43e,"callback-polling");
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
this.openTunnelWith=function(_441,url){
dojo.io.bind({url:(url||cometd.url),content:_441,transport:"ScriptSrcTransport",jsonParamName:"jsonp",load:dojo.lang.hitch(this,function(type,data,evt,args){
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
this.sendMessage=function(_447,_448){
if((_448)||(this.connected)){
_447.connectionId=this.connectionId;
_447.clientId=cometd.clientId;
var _449={url:cometd.url||djConfig["cometdRoot"],transport:"ScriptSrcTransport",jsonParamName:"jsonp",content:{message:dojo.json.serialize([_447])}};
return dojo.io.bind(_449);
}else{
this.backlog.push(_447);
}
};
this.startup=function(_44a){
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
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_451){
if(_451){
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
},blend:function(_452,_453){
var rgb=null;
if(dojo.lang.isArray(_452)){
rgb=_452;
}else{
if(_452 instanceof dojo.gfx.color.Color){
rgb=_452.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_452).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_453);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_457){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_457);
}
if(!_457){
_457=0;
}
_457=Math.min(Math.max(-1,_457),1);
_457=((_457+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_457));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_45c){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_45c));
};
dojo.gfx.color.extractRGB=function(_45d){
var hex="0123456789abcdef";
_45d=_45d.toLowerCase();
if(_45d.indexOf("rgb")==0){
var _45f=_45d.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_45f.splice(1,3);
return ret;
}else{
var _461=dojo.gfx.color.hex2rgb(_45d);
if(_461){
return _461;
}else{
return dojo.gfx.color.named[_45d]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _463="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_463+"]","g"),"")!=""){
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
rgb[i]=_463.indexOf(rgb[i].charAt(0))*16+_463.indexOf(rgb[i].charAt(1));
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
dojo.provide("dojo.lfx.Line");
dojo.lfx.Line=function(_46c,end){
this.start=_46c;
this.end=end;
if(dojo.lang.isArray(_46c)){
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
var diff=end-_46c;
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
dojo.debug(ret);
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
dojo.lang.extend(dojo.lfx.IAnimation,{curve:null,duration:1000,easing:null,repeatCount:0,rate:25,handler:null,beforeBegin:null,onBegin:null,onAnimate:null,onEnd:null,onPlay:null,onPause:null,onStop:null,play:null,pause:null,stop:null,connect:function(evt,_47b,_47c){
if(!_47c){
_47c=_47b;
_47b=this;
}
_47c=dojo.lang.hitch(_47b,_47c);
var _47d=this[evt]||function(){
};
this[evt]=function(){
var ret=_47d.apply(this,arguments);
_47c.apply(this,arguments);
return ret;
};
return this;
},fire:function(evt,args){
if(this[evt]){
this[evt].apply(this,(args||[]));
}
return this;
},repeat:function(_481){
this.repeatCount=_481;
return this;
},_active:false,_paused:false});
dojo.lfx.Animation=function(_482,_483,_484,_485,_486,rate){
dojo.lfx.IAnimation.call(this);
if(dojo.lang.isNumber(_482)||(!_482&&_483.getValue)){
rate=_486;
_486=_485;
_485=_484;
_484=_483;
_483=_482;
_482=null;
}else{
if(_482.getValue||dojo.lang.isArray(_482)){
rate=_485;
_486=_484;
_485=_483;
_484=_482;
_483=null;
_482=null;
}
}
if(dojo.lang.isArray(_484)){
this.curve=new dojo.lfx.Line(_484[0],_484[1]);
}else{
this.curve=_484;
}
if(_483!=null&&_483>0){
this.duration=_483;
}
if(_486){
this.repeatCount=_486;
}
if(rate){
this.rate=rate;
}
if(_482){
dojo.lang.forEach(["handler","beforeBegin","onBegin","onEnd","onPlay","onStop","onAnimate"],function(item){
if(_482[item]){
this.connect(item,_482[item]);
}
},this);
}
if(_485&&dojo.lang.isFunction(_485)){
this.easing=_485;
}
};
dojo.inherits(dojo.lfx.Animation,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Animation,{_startTime:null,_endTime:null,_timer:null,_percent:0,_startRepeatCount:0,play:function(_489,_48a){
if(_48a){
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
if(_489>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_48a);
}),_489);
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
var _48c=this.curve.getValue(step);
if(this._percent==0){
if(!this._startRepeatCount){
this._startRepeatCount=this.repeatCount;
}
this.fire("handler",["begin",_48c]);
this.fire("onBegin",[_48c]);
}
this.fire("handler",["play",_48c]);
this.fire("onPlay",[_48c]);
this._cycle();
return this;
},pause:function(){
clearTimeout(this._timer);
if(!this._active){
return this;
}
this._paused=true;
var _48d=this.curve.getValue(this._percent/100);
this.fire("handler",["pause",_48d]);
this.fire("onPause",[_48d]);
return this;
},gotoPercent:function(pct,_48f){
clearTimeout(this._timer);
this._active=true;
this._paused=true;
this._percent=pct;
if(_48f){
this.play();
}
return this;
},stop:function(_490){
clearTimeout(this._timer);
var step=this._percent/100;
if(_490){
step=1;
}
var _492=this.curve.getValue(step);
this.fire("handler",["stop",_492]);
this.fire("onStop",[_492]);
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
var _495=this.curve.getValue(step);
this.fire("handler",["animate",_495]);
this.fire("onAnimate",[_495]);
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
dojo.lfx.Combine=function(){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._animsEnded=0;
var _496=arguments;
if(_496.length==1&&(dojo.lang.isArray(_496[0])||dojo.lang.isArrayLike(_496[0]))){
_496=_496[0];
}
dojo.lang.forEach(_496,function(anim){
this._anims.push(anim);
anim.connect("onEnd",dojo.lang.hitch(this,"_onAnimsEnded"));
},this);
};
dojo.inherits(dojo.lfx.Combine,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Combine,{_animsEnded:0,play:function(_498,_499){
if(!this._anims.length){
return this;
}
this.fire("beforeBegin");
if(_498>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_499);
}),_498);
return this;
}
if(_499||this._anims[0].percent==0){
this.fire("onBegin");
}
this.fire("onPlay");
this._animsCall("play",null,_499);
return this;
},pause:function(){
this.fire("onPause");
this._animsCall("pause");
return this;
},stop:function(_49a){
this.fire("onStop");
this._animsCall("stop",_49a);
return this;
},_onAnimsEnded:function(){
this._animsEnded++;
if(this._animsEnded>=this._anims.length){
this.fire("onEnd");
}
return this;
},_animsCall:function(_49b){
var args=[];
if(arguments.length>1){
for(var i=1;i<arguments.length;i++){
args.push(arguments[i]);
}
}
var _49e=this;
dojo.lang.forEach(this._anims,function(anim){
anim[_49b](args);
},_49e);
return this;
}});
dojo.lfx.Chain=function(){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._currAnim=-1;
var _4a0=arguments;
if(_4a0.length==1&&(dojo.lang.isArray(_4a0[0])||dojo.lang.isArrayLike(_4a0[0]))){
_4a0=_4a0[0];
}
var _4a1=this;
dojo.lang.forEach(_4a0,function(anim,i,_4a4){
this._anims.push(anim);
if(i<_4a4.length-1){
anim.connect("onEnd",dojo.lang.hitch(this,"_playNext"));
}else{
anim.connect("onEnd",dojo.lang.hitch(this,function(){
this.fire("onEnd");
}));
}
},this);
};
dojo.inherits(dojo.lfx.Chain,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Chain,{_currAnim:-1,play:function(_4a5,_4a6){
if(!this._anims.length){
return this;
}
if(_4a6||!this._anims[this._currAnim]){
this._currAnim=0;
}
var _4a7=this._anims[this._currAnim];
this.fire("beforeBegin");
if(_4a5>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_4a6);
}),_4a5);
return this;
}
if(_4a7){
if(this._currAnim==0){
this.fire("handler",["begin",this._currAnim]);
this.fire("onBegin",[this._currAnim]);
}
this.fire("onPlay",[this._currAnim]);
_4a7.play(null,_4a6);
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
var _4a8=this._anims[this._currAnim];
if(_4a8){
if(!_4a8._active||_4a8._paused){
this.play();
}else{
this.pause();
}
}
return this;
},stop:function(){
var _4a9=this._anims[this._currAnim];
if(_4a9){
_4a9.stop();
this.fire("onStop",[this._currAnim]);
}
return _4a9;
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
dojo.lfx.combine=function(){
var _4aa=arguments;
if(dojo.lang.isArray(arguments[0])){
_4aa=arguments[0];
}
if(_4aa.length==1){
return _4aa[0];
}
return new dojo.lfx.Combine(_4aa);
};
dojo.lfx.chain=function(){
var _4ab=arguments;
if(dojo.lang.isArray(arguments[0])){
_4ab=arguments[0];
}
if(_4ab.length==1){
return _4ab[0];
}
return new dojo.lfx.Chain(_4ab);
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
dojo.html.hasClass=function(node,_4b1){
return (new RegExp("(^|\\s+)"+_4b1+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_4b3){
_4b3+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_4b3);
};
dojo.html.addClass=function(node,_4b5){
if(dojo.html.hasClass(node,_4b5)){
return false;
}
_4b5=(dojo.html.getClass(node)+" "+_4b5).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_4b5);
};
dojo.html.setClass=function(node,_4b7){
node=dojo.byId(node);
var cs=new String(_4b7);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_4b7);
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
dojo.html.removeClass=function(node,_4ba,_4bb){
try{
if(!_4bb){
var _4bc=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_4ba+"(\\s+|$)"),"$1$2");
}else{
var _4bc=dojo.html.getClass(node).replace(_4ba,"");
}
dojo.html.setClass(node,_4bc);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_4be,_4bf){
dojo.html.removeClass(node,_4bf);
dojo.html.addClass(node,_4be);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_4c0,_4c1,_4c2,_4c3,_4c4){
var _4c5=dojo.doc();
_4c1=dojo.byId(_4c1)||_4c5;
var _4c6=_4c0.split(/\s+/g);
var _4c7=[];
if(_4c3!=1&&_4c3!=2){
_4c3=0;
}
var _4c8=new RegExp("(\\s|^)(("+_4c6.join(")|(")+"))(\\s|$)");
var _4c9=_4c6.join(" ").length;
var _4ca=[];
if(!_4c4&&_4c5.evaluate){
var _4cb=".//"+(_4c2||"*")+"[contains(";
if(_4c3!=dojo.html.classMatchType.ContainsAny){
_4cb+="concat(' ',@class,' '), ' "+_4c6.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_4c3==2){
_4cb+=" and string-length(@class)="+_4c9+"]";
}else{
_4cb+="]";
}
}else{
_4cb+="concat(' ',@class,' '), ' "+_4c6.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _4cc=_4c5.evaluate(_4cb,_4c1,null,XPathResult.ANY_TYPE,null);
var _4cd=_4cc.iterateNext();
while(_4cd){
try{
_4ca.push(_4cd);
_4cd=_4cc.iterateNext();
}
catch(e){
break;
}
}
return _4ca;
}else{
if(!_4c2){
_4c2="*";
}
_4ca=_4c1.getElementsByTagName(_4c2);
var node,i=0;
outer:
while(node=_4ca[i++]){
var _4cf=dojo.html.getClasses(node);
if(_4cf.length==0){
continue outer;
}
var _4d0=0;
for(var j=0;j<_4cf.length;j++){
if(_4c8.test(_4cf[j])){
if(_4c3==dojo.html.classMatchType.ContainsAny){
_4c7.push(node);
continue outer;
}else{
_4d0++;
}
}else{
if(_4c3==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_4d0==_4c6.length){
if((_4c3==dojo.html.classMatchType.IsOnly)&&(_4d0==_4cf.length)){
_4c7.push(node);
}else{
if(_4c3==dojo.html.classMatchType.ContainsAll){
_4c7.push(node);
}
}
}
}
return _4c7;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_4d2){
var arr=_4d2.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_4d5){
return _4d5.replace(/([A-Z])/g,"-$1").toLowerCase();
};
dojo.html.getComputedStyle=function(node,_4d7,_4d8){
node=dojo.byId(node);
var _4d7=dojo.html.toSelectorCase(_4d7);
var _4d9=dojo.html.toCamelCase(_4d7);
if(!node||!node.style){
return _4d8;
}else{
if(document.defaultView&&dojo.dom.isDescendantOf(node,node.ownerDocument)){
try{
var cs=document.defaultView.getComputedStyle(node,"");
if(cs){
return cs.getPropertyValue(_4d7);
}
}
catch(e){
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_4d7);
}else{
return _4d8;
}
}
}else{
if(node.currentStyle){
return node.currentStyle[_4d9];
}
}
}
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_4d7);
}else{
return _4d8;
}
};
dojo.html.getStyleProperty=function(node,_4dc){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_4dc)]:undefined);
};
dojo.html.getStyle=function(node,_4de){
var _4df=dojo.html.getStyleProperty(node,_4de);
return (_4df?_4df:dojo.html.getComputedStyle(node,_4de));
};
dojo.html.setStyle=function(node,_4e1,_4e2){
node=dojo.byId(node);
if(node&&node.style){
var _4e3=dojo.html.toCamelCase(_4e1);
node.style[_4e3]=_4e2;
}
};
dojo.html.setStyleText=function(_4e4,text){
try{
_4e4.style.cssText=text;
}
catch(e){
_4e4.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_4e6,_4e7){
if(!_4e7.style.cssText){
_4e6.setAttribute("style",_4e7.getAttribute("style"));
}else{
_4e6.style.cssText=_4e7.style.cssText;
}
dojo.html.addClass(_4e6,dojo.html.getClass(_4e7));
};
dojo.html.getUnitValue=function(node,_4e9,_4ea){
var s=dojo.html.getComputedStyle(node,_4e9);
if((!s)||((s=="auto")&&(_4ea))){
return {value:0,units:"px"};
}
var _4ec=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_4ec){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_4ec[1]),units:_4ec[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
dojo.html.getPixelValue=function(node,_4ee,_4ef){
var _4f0=dojo.html.getUnitValue(node,_4ee,_4ef);
if(isNaN(_4f0.value)){
return 0;
}
if((_4f0.value)&&(_4f0.units!="px")){
return NaN;
}
return _4f0.value;
};
dojo.html.setPositivePixelValue=function(node,_4f2,_4f3){
if(isNaN(_4f3)){
return false;
}
node.style[_4f2]=Math.max(0,_4f3)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_4f4,_4f5,_4f6){
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
_4f6=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_4f6=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_4f4+" { "+_4f5+" }";
return dojo.html.styleSheet.insertRule(rule,_4f6);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_4f4,_4f5,_4f6);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_4f8){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_4f8){
_4f8=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_4f8);
}
}else{
if(document.styleSheets[0]){
if(!_4f8){
_4f8=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_4f8);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_4fb){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _4fc=dojo.hostenv.getText(URI);
_4fc=dojo.html.fixPathsInCssText(_4fc,URI);
if(_4fb){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_4fc)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _4ff=doc.getElementsByTagName("style");
for(var i=0;i<_4ff.length;i++){
if(_4ff[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _500=dojo.html.insertCssText(_4fc);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_4fc,"nodeRef":_500});
if(_500&&djConfig.isDebug){
_500.setAttribute("dbgHref",URI);
}
return _500;
};
dojo.html.insertCssText=function(_501,doc,URI){
if(!_501){
return;
}
if(!doc){
doc=document;
}
if(URI){
_501=dojo.html.fixPathsInCssText(_501,URI);
}
var _504=doc.createElement("style");
_504.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_504);
}
if(_504.styleSheet){
_504.styleSheet.cssText=_501;
}else{
var _506=doc.createTextNode(_501);
_504.appendChild(_506);
}
return _504;
};
dojo.html.fixPathsInCssText=function(_507,URI){
function iefixPathsInCssText(){
var _509=/AlphaImageLoader\(src\=['"]([\t\s\w()\/.\\'"-:#=&?]*)['"]/;
while(match=_509.exec(_507)){
url=match[1].replace(regexTrim,"$2");
if(!regexProtocol.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_507.substring(0,match.index)+"AlphaImageLoader(src='"+url+"'";
_507=_507.substr(match.index+match[0].length);
}
return str+_507;
}
if(!_507||!URI){
return;
}
var _50a,str="",url="";
var _50b=/url\(\s*([\t\s\w()\/.\\'"-:#=&?]+)\s*\)/;
var _50c=/(file|https?|ftps?):\/\//;
var _50d=/^[\s]*(['"]?)([\w()\/.\\'"-:#=&?]*)\1[\s]*?$/;
if(dojo.render.html.ie55||dojo.render.html.ie60){
_507=iefixPathsInCssText();
}
while(_50a=_50b.exec(_507)){
url=_50a[1].replace(_50d,"$2");
if(!_50c.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_507.substring(0,_50a.index)+"url("+url+")";
_507=_507.substr(_50a.index+_50a[0].length);
}
return str+_507;
};
dojo.html.setActiveStyleSheet=function(_50e){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_50e){
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
var _514={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _514){
if(_514[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.provide("dojo.html.display");
dojo.html._toggle=function(node,_517,_518){
node=dojo.byId(node);
_518(node,!_517(node));
return _517(node);
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
dojo.html.setShowing=function(node,_51d){
dojo.html[(_51d?"show":"hide")](node);
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
dojo.html.setDisplay=function(node,_523){
dojo.html.setStyle(node,"display",((_523 instanceof String||typeof _523=="string")?_523:(_523?dojo.html.suggestDisplayByTagName(node):"none")));
};
dojo.html.isDisplayed=function(node){
return (dojo.html.getComputedStyle(node,"display")!="none");
};
dojo.html.toggleDisplay=function(node){
return dojo.html._toggle(node,dojo.html.isDisplayed,dojo.html.setDisplay);
};
dojo.html.setVisibility=function(node,_527){
dojo.html.setStyle(node,"visibility",((_527 instanceof String||typeof _527=="string")?_527:(_527?"visible":"hidden")));
};
dojo.html.isVisible=function(node){
return (dojo.html.getComputedStyle(node,"visibility")!="hidden");
};
dojo.html.toggleVisibility=function(node){
return dojo.html._toggle(node,dojo.html.isVisible,dojo.html.setVisibility);
};
dojo.html.setOpacity=function(node,_52b,_52c){
node=dojo.byId(node);
var h=dojo.render.html;
if(!_52c){
if(_52b>=1){
if(h.ie){
dojo.html.clearOpacity(node);
return;
}else{
_52b=0.999999;
}
}else{
if(_52b<0){
_52b=0;
}
}
}
if(h.ie){
if(node.nodeName.toLowerCase()=="tr"){
var tds=node.getElementsByTagName("td");
for(var x=0;x<tds.length;x++){
tds[x].style.filter="Alpha(Opacity="+_52b*100+")";
}
}
node.style.filter="Alpha(Opacity="+_52b*100+")";
}else{
if(h.moz){
node.style.opacity=_52b;
node.style.MozOpacity=_52b;
}else{
if(h.safari){
node.style.opacity=_52b;
node.style.KhtmlOpacity=_52b;
}else{
node.style.opacity=_52b;
}
}
}
};
dojo.html.clearOpacity=function clearOpacity(node){
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
dojo.html.getOpacity=function getOpacity(node){
node=dojo.byId(node);
var h=dojo.render.html;
if(h.ie){
var opac=(node.filters&&node.filters.alpha&&typeof node.filters.alpha.opacity=="number"?node.filters.alpha.opacity:100)/100;
}else{
var opac=node.style.opacity||node.style.MozOpacity||node.style.KhtmlOpacity||1;
}
return opac>=0.999999?1:Number(opac);
};
dojo.provide("dojo.html.color");
dojo.html.getBackgroundColor=function(node){
node=dojo.byId(node);
var _537;
do{
_537=dojo.html.getStyle(node,"background-color");
if(_537.toLowerCase()=="rgba(0, 0, 0, 0)"){
_537="transparent";
}
if(node==document.getElementsByTagName("body")[0]){
node=null;
break;
}
node=node.parentNode;
}while(node&&dojo.lang.inArray(["transparent",""],_537));
if(_537=="transparent"){
_537=[255,255,255,0];
}else{
_537=dojo.gfx.color.extractRGB(_537);
}
return _537;
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
var _53a=dojo.global();
var _53b=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_53b.documentElement.clientWidth;
h=_53a.innerHeight;
}else{
if(!dojo.render.html.opera&&_53a.innerWidth){
w=_53a.innerWidth;
h=_53a.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_53b,"documentElement.clientWidth")){
var w2=_53b.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_53b.documentElement.clientHeight;
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
var _53f=dojo.global();
var _540=dojo.doc();
var top=_53f.pageYOffset||_540.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_53f.pageXOffset||_540.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _545=dojo.doc();
var _546=dojo.byId(node);
type=type.toLowerCase();
while((_546)&&(_546.nodeName.toLowerCase()!=type)){
if(_546==(_545["body"]||_545["documentElement"])){
return null;
}
_546=_546.parentNode;
}
return _546;
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
var _54e={x:0,y:0};
if(e.pageX||e.pageY){
_54e.x=e.pageX;
_54e.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_54e.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_54e.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _54e;
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
if(dojo.render.html.ie){
if(window.location.href.substr(0,6).toLowerCase()!="https:"){
(function(){
var _553=dojo.doc().createElement("script");
_553.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_553);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.html._callDeprecated=function(_556,_557,args,_559,_55a){
dojo.deprecated("dojo.html."+_556,"replaced by dojo.html."+_557+"("+(_559?"node, {"+_559+": "+_559+"}":"")+")"+(_55a?"."+_55a:""),"0.5");
var _55b=[];
if(_559){
var _55c={};
_55c[_559]=args[1];
_55b.push(args[0]);
_55b.push(_55c);
}else{
_55b=args;
}
var ret=dojo.html[_557].apply(dojo.html,args);
if(_55a){
return ret[_55a];
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
dojo.provide("dojo.html.layout");
dojo.html.sumAncestorProperties=function(node,prop){
node=dojo.byId(node);
if(!node){
return 0;
}
var _560=0;
while(node){
if(dojo.html.getComputedStyle(node,"position")=="fixed"){
return 0;
}
var val=node[prop];
if(val){
_560+=val-0;
if(node==dojo.body()){
break;
}
}
node=node.parentNode;
}
return _560;
};
dojo.html.setStyleAttributes=function(node,_563){
node=dojo.byId(node);
var _564=_563.replace(/(;)?\s*$/,"").split(";");
for(var i=0;i<_564.length;i++){
var _566=_564[i].split(":");
var name=_566[0].replace(/\s*$/,"").replace(/^\s*/,"").toLowerCase();
var _568=_566[1].replace(/\s*$/,"").replace(/^\s*/,"");
switch(name){
case "opacity":
dojo.html.setOpacity(node,_568);
break;
case "content-height":
dojo.html.setContentBox(node,{height:_568});
break;
case "content-width":
dojo.html.setContentBox(node,{width:_568});
break;
case "outer-height":
dojo.html.setMarginBox(node,{height:_568});
break;
case "outer-width":
dojo.html.setMarginBox(node,{width:_568});
break;
default:
node.style[dojo.html.toCamelCase(name)]=_568;
}
}
};
dojo.html.boxSizing={MARGIN_BOX:"margin-box",BORDER_BOX:"border-box",PADDING_BOX:"padding-box",CONTENT_BOX:"content-box"};
dojo.html.getAbsolutePosition=dojo.html.abs=function(node,_56a,_56b){
node=dojo.byId(node,node.ownerDocument);
var ret={x:0,y:0};
var bs=dojo.html.boxSizing;
if(!_56b){
_56b=bs.CONTENT_BOX;
}
var _56e=2;
var _56f;
switch(_56b){
case bs.MARGIN_BOX:
_56f=3;
break;
case bs.BORDER_BOX:
_56f=2;
break;
case bs.PADDING_BOX:
default:
_56f=1;
break;
case bs.CONTENT_BOX:
_56f=0;
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
_56e=1;
try{
var bo=document.getBoxObjectFor(node);
ret.x=bo.x-dojo.html.sumAncestorProperties(node,"scrollLeft");
ret.y=bo.y-dojo.html.sumAncestorProperties(node,"scrollTop");
}
catch(e){
}
}else{
if(node["offsetParent"]){
var _573;
if((h.safari)&&(node.style.getPropertyValue("position")=="absolute")&&(node.parentNode==db)){
_573=db;
}else{
_573=db.parentNode;
}
if(node.parentNode!=db){
var nd=node;
if(dojo.render.html.opera){
nd=db;
}
ret.x-=dojo.html.sumAncestorProperties(nd,"scrollLeft");
ret.y-=dojo.html.sumAncestorProperties(nd,"scrollTop");
}
var _575=node;
do{
var n=_575["offsetLeft"];
if(!h.opera||n>0){
ret.x+=isNaN(n)?0:n;
}
var m=_575["offsetTop"];
ret.y+=isNaN(m)?0:m;
_575=_575.offsetParent;
}while((_575!=_573)&&(_575!=null));
}else{
if(node["x"]&&node["y"]){
ret.x+=isNaN(node.x)?0:node.x;
ret.y+=isNaN(node.y)?0:node.y;
}
}
}
}
if(_56a){
var _578=dojo.html.getScroll();
ret.y+=_578.top;
ret.x+=_578.left;
}
var _579=[dojo.html.getPaddingExtent,dojo.html.getBorderExtent,dojo.html.getMarginExtent];
if(_56e>_56f){
for(var i=_56f;i<_56e;++i){
ret.y+=_579[i](node,"top");
ret.x+=_579[i](node,"left");
}
}else{
if(_56e<_56f){
for(var i=_56f;i>_56e;--i){
ret.y-=_579[i-1](node,"top");
ret.x-=_579[i-1](node,"left");
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
dojo.html._sumPixelValues=function(node,_57d,_57e){
var _57f=0;
for(var x=0;x<_57d.length;x++){
_57f+=dojo.html.getPixelValue(node,_57d[x],_57e);
}
return _57f;
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
var _58c=dojo.html.getBorder(node);
return {width:pad.width+_58c.width,height:pad.height+_58c.height};
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
var _591=dojo.html.getStyle(node,"-moz-box-sizing");
if(!_591){
_591=dojo.html.getStyle(node,"box-sizing");
}
return (_591?_591:bs.CONTENT_BOX);
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
var _596=dojo.html.getBorder(node);
return {width:box.width-_596.width,height:box.height-_596.height};
};
dojo.html.getContentBox=function(node){
node=dojo.byId(node);
var _598=dojo.html.getPadBorder(node);
return {width:node.offsetWidth-_598.width,height:node.offsetHeight-_598.height};
};
dojo.html.setContentBox=function(node,args){
node=dojo.byId(node);
var _59b=0;
var _59c=0;
var isbb=dojo.html.isBorderBox(node);
var _59e=(isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var ret={};
if(typeof args.width!="undefined"){
_59b=args.width+_59e.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_59b);
}
if(typeof args.height!="undefined"){
_59c=args.height+_59e.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_59c);
}
return ret;
};
dojo.html.getMarginBox=function(node){
var _5a1=dojo.html.getBorderBox(node);
var _5a2=dojo.html.getMargin(node);
return {width:_5a1.width+_5a2.width,height:_5a1.height+_5a2.height};
};
dojo.html.setMarginBox=function(node,args){
node=dojo.byId(node);
var _5a5=0;
var _5a6=0;
var isbb=dojo.html.isBorderBox(node);
var _5a8=(!isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var _5a9=dojo.html.getMargin(node);
var ret={};
if(typeof args.width!="undefined"){
_5a5=args.width-_5a8.width;
_5a5-=_5a9.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_5a5);
}
if(typeof args.height!="undefined"){
_5a6=args.height-_5a8.height;
_5a6-=_5a9.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_5a6);
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
dojo.html.toCoordinateObject=dojo.html.toCoordinateArray=function(_5ae,_5af,_5b0){
if(_5ae instanceof Array||typeof _5ae=="array"){
dojo.deprecated("dojo.html.toCoordinateArray","use dojo.html.toCoordinateObject({left: , top: , width: , height: }) instead","0.5");
while(_5ae.length<4){
_5ae.push(0);
}
while(_5ae.length>4){
_5ae.pop();
}
var ret={left:_5ae[0],top:_5ae[1],width:_5ae[2],height:_5ae[3]};
}else{
if(!_5ae.nodeType&&!(_5ae instanceof String||typeof _5ae=="string")&&("width" in _5ae||"height" in _5ae||"left" in _5ae||"x" in _5ae||"top" in _5ae||"y" in _5ae)){
var ret={left:_5ae.left||_5ae.x||0,top:_5ae.top||_5ae.y||0,width:_5ae.width||0,height:_5ae.height||0};
}else{
var node=dojo.byId(_5ae);
var pos=dojo.html.abs(node,_5af,_5b0);
var _5b4=dojo.html.getMarginBox(node);
var ret={left:pos.left,top:pos.top,width:_5b4.width,height:_5b4.height};
}
}
ret.x=ret.left;
ret.y=ret.top;
return ret;
};
dojo.html.setMarginBoxWidth=dojo.html.setOuterWidth=function(node,_5b6){
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
dojo.html.getTotalOffset=function(node,type,_5b9){
return dojo.html._callDeprecated("getTotalOffset","getAbsolutePosition",arguments,null,type);
};
dojo.html.getAbsoluteX=function(node,_5bb){
return dojo.html._callDeprecated("getAbsoluteX","getAbsolutePosition",arguments,null,"x");
};
dojo.html.getAbsoluteY=function(node,_5bd){
return dojo.html._callDeprecated("getAbsoluteY","getAbsolutePosition",arguments,null,"y");
};
dojo.html.totalOffsetLeft=function(node,_5bf){
return dojo.html._callDeprecated("totalOffsetLeft","getAbsolutePosition",arguments,null,"left");
};
dojo.html.totalOffsetTop=function(node,_5c1){
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
dojo.html.setContentBoxWidth=dojo.html.setContentWidth=function(node,_5cb){
return dojo.html._callDeprecated("setContentBoxWidth","setContentBox",arguments,"width");
};
dojo.html.setContentBoxHeight=dojo.html.setContentHeight=function(node,_5cd){
return dojo.html._callDeprecated("setContentBoxHeight","setContentBox",arguments,"height");
};
dojo.provide("dojo.lfx.html");
dojo.lfx.html._byId=function(_5ce){
if(!_5ce){
return [];
}
if(dojo.lang.isArrayLike(_5ce)){
if(!_5ce.alreadyChecked){
var n=[];
dojo.lang.forEach(_5ce,function(node){
n.push(dojo.byId(node));
});
n.alreadyChecked=true;
return n;
}else{
return _5ce;
}
}else{
var n=[];
n.push(dojo.byId(_5ce));
n.alreadyChecked=true;
return n;
}
};
dojo.lfx.html.propertyAnimation=function(_5d1,_5d2,_5d3,_5d4,_5d5){
_5d1=dojo.lfx.html._byId(_5d1);
var _5d6={"propertyMap":_5d2,"nodes":_5d1,"duration":_5d3,"easing":_5d4||dojo.lfx.easeDefault};
var _5d7=function(args){
if(args.nodes.length==1){
var pm=args.propertyMap;
if(!dojo.lang.isArray(args.propertyMap)){
var parr=[];
for(var _5db in pm){
pm[_5db].property=_5db;
parr.push(pm[_5db]);
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
var _5dd=function(_5de){
var _5df=[];
dojo.lang.forEach(_5de,function(c){
_5df.push(Math.round(c));
});
return _5df;
};
var _5e1=function(n,_5e3){
n=dojo.byId(n);
if(!n||!n.style){
return;
}
for(var s in _5e3){
if(s=="opacity"){
dojo.html.setOpacity(n,_5e3[s]);
}else{
n.style[s]=_5e3[s];
}
}
};
var _5e5=function(_5e6){
this._properties=_5e6;
this.diffs=new Array(_5e6.length);
dojo.lang.forEach(_5e6,function(prop,i){
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
var _5ed=null;
if(dojo.lang.isArray(prop.start)){
}else{
if(prop.start instanceof dojo.gfx.color.Color){
_5ed=(prop.units||"rgb")+"(";
for(var j=0;j<prop.startRgb.length;j++){
_5ed+=Math.round(((prop.endRgb[j]-prop.startRgb[j])*n)+prop.startRgb[j])+(j<prop.startRgb.length-1?",":"");
}
_5ed+=")";
}else{
_5ed=((this.diffs[i])*n)+prop.start+(prop.property!="opacity"?prop.units||"px":"");
}
}
ret[dojo.html.toCamelCase(prop.property)]=_5ed;
},this);
return ret;
};
};
var anim=new dojo.lfx.Animation({beforeBegin:function(){
_5d7(_5d6);
anim.curve=new _5e5(_5d6.propertyMap);
},onAnimate:function(_5f0){
dojo.lang.forEach(_5d6.nodes,function(node){
_5e1(node,_5f0);
});
}},_5d6.duration,null,_5d6.easing);
if(_5d5){
for(var x in _5d5){
if(dojo.lang.isFunction(_5d5[x])){
anim.connect(x,anim,_5d5[x]);
}
}
}
return anim;
};
dojo.lfx.html._makeFadeable=function(_5f3){
var _5f4=function(node){
if(dojo.render.html.ie){
if((node.style.zoom.length==0)&&(dojo.html.getStyle(node,"zoom")=="normal")){
node.style.zoom="1";
}
if((node.style.width.length==0)&&(dojo.html.getStyle(node,"width")=="auto")){
node.style.width="auto";
}
}
};
if(dojo.lang.isArrayLike(_5f3)){
dojo.lang.forEach(_5f3,_5f4);
}else{
_5f4(_5f3);
}
};
dojo.lfx.html.fade=function(_5f6,_5f7,_5f8,_5f9,_5fa){
_5f6=dojo.lfx.html._byId(_5f6);
var _5fb={property:"opacity"};
if(!dj_undef("start",_5f7)){
_5fb.start=_5f7.start;
}else{
_5fb.start=function(){
return dojo.html.getOpacity(_5f6[0]);
};
}
if(!dj_undef("end",_5f7)){
_5fb.end=_5f7.end;
}else{
dojo.raise("dojo.lfx.html.fade needs an end value");
}
var anim=dojo.lfx.propertyAnimation(_5f6,[_5fb],_5f8,_5f9);
anim.connect("beforeBegin",function(){
dojo.lfx.html._makeFadeable(_5f6);
});
if(_5fa){
anim.connect("onEnd",function(){
_5fa(_5f6,anim);
});
}
return anim;
};
dojo.lfx.html.fadeIn=function(_5fd,_5fe,_5ff,_600){
return dojo.lfx.html.fade(_5fd,{end:1},_5fe,_5ff,_600);
};
dojo.lfx.html.fadeOut=function(_601,_602,_603,_604){
return dojo.lfx.html.fade(_601,{end:0},_602,_603,_604);
};
dojo.lfx.html.fadeShow=function(_605,_606,_607,_608){
_605=dojo.lfx.html._byId(_605);
dojo.lang.forEach(_605,function(node){
dojo.html.setOpacity(node,0);
});
var anim=dojo.lfx.html.fadeIn(_605,_606,_607,_608);
anim.connect("beforeBegin",function(){
if(dojo.lang.isArrayLike(_605)){
dojo.lang.forEach(_605,dojo.html.show);
}else{
dojo.html.show(_605);
}
});
return anim;
};
dojo.lfx.html.fadeHide=function(_60b,_60c,_60d,_60e){
var anim=dojo.lfx.html.fadeOut(_60b,_60c,_60d,function(){
if(dojo.lang.isArrayLike(_60b)){
dojo.lang.forEach(_60b,dojo.html.hide);
}else{
dojo.html.hide(_60b);
}
if(_60e){
_60e(_60b,anim);
}
});
return anim;
};
dojo.lfx.html.wipeIn=function(_610,_611,_612,_613){
_610=dojo.lfx.html._byId(_610);
var _614=[];
dojo.lang.forEach(_610,function(node){
var _616={overflow:null};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:0,end:function(){
return node.scrollHeight;
}}},_611,_612);
anim.connect("beforeBegin",function(){
_616.overflow=dojo.html.getStyle(node,"overflow");
with(node.style){
if(_616.overflow=="visible"){
overflow="hidden";
}
visibility="visible";
height="0px";
}
dojo.html.show(node);
});
anim.connect("onEnd",function(){
with(node.style){
overflow=_616.overflow;
height="";
visibility="visible";
}
if(_613){
_613(node,anim);
}
});
_614.push(anim);
});
return dojo.lfx.combine(_614);
};
dojo.lfx.html.wipeOut=function(_618,_619,_61a,_61b){
_618=dojo.lfx.html._byId(_618);
var _61c=[];
dojo.lang.forEach(_618,function(node){
var _61e={overflow:null};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:function(){
return dojo.html.getContentBox(node).height;
},end:0}},_619,_61a,{"beforeBegin":function(){
_61e.overflow=dojo.html.getStyle(node,"overflow");
if(_61e.overflow=="visible"){
node.style.overflow="hidden";
}
node.style.visibility="visible";
dojo.html.show(node);
},"onEnd":function(){
with(node.style){
overflow=_61e.overflow;
visibility="hidden";
height="";
}
if(_61b){
_61b(node,anim);
}
}});
_61c.push(anim);
});
return dojo.lfx.combine(_61c);
};
dojo.lfx.html.slideTo=function(_620,_621,_622,_623,_624){
_620=dojo.lfx.html._byId(_620);
var _625=[];
var _626=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_621)){
dojo.deprecated("dojo.lfx.html.slideTo(node, array)","use dojo.lfx.html.slideTo(node, {top: value, left: value});","0.5");
_621={top:_621[0],left:_621[1]};
}
dojo.lang.forEach(_620,function(node){
var top=null;
var left=null;
var init=(function(){
var _62b=node;
return function(){
var pos=_626(_62b,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_626(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_626(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_62b,true);
dojo.html.setStyleAttributes(_62b,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:(_621.top||0)},"left":{start:left,end:(_621.left||0)}},_622,_623,{"beforeBegin":init});
if(_624){
anim.connect("onEnd",function(){
_624(_620,anim);
});
}
_625.push(anim);
});
return dojo.lfx.combine(_625);
};
dojo.lfx.html.slideBy=function(_62f,_630,_631,_632,_633){
_62f=dojo.lfx.html._byId(_62f);
var _634=[];
var _635=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_630)){
dojo.deprecated("dojo.lfx.html.slideBy(node, array)","use dojo.lfx.html.slideBy(node, {top: value, left: value});","0.5");
_630={top:_630[0],left:_630[1]};
}
dojo.lang.forEach(_62f,function(node){
var top=null;
var left=null;
var init=(function(){
var _63a=node;
return function(){
var pos=_635(_63a,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_635(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_635(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_63a,true);
dojo.html.setStyleAttributes(_63a,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:top+(_630.top||0)},"left":{start:left,end:left+(_630.left||0)}},_631,_632).connect("beforeBegin",init);
if(_633){
anim.connect("onEnd",function(){
_633(_62f,anim);
});
}
_634.push(anim);
});
return dojo.lfx.combine(_634);
};
dojo.lfx.html.explode=function(_63e,_63f,_640,_641,_642){
var h=dojo.html;
_63e=dojo.byId(_63e);
_63f=dojo.byId(_63f);
var _644=h.toCoordinateObject(_63e,true);
var _645=document.createElement("div");
h.copyStyle(_645,_63f);
with(_645.style){
position="absolute";
display="none";
}
dojo.body().appendChild(_645);
with(_63f.style){
visibility="hidden";
display="block";
}
var _646=h.toCoordinateObject(_63f,true);
_645.style.backgroundColor=h.getStyle(_63f,"background-color").toLowerCase();
with(_63f.style){
display="none";
visibility="visible";
}
var _647={opacity:{start:0.5,end:1}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_647[type]={start:_644[type],end:_646[type]};
});
var anim=new dojo.lfx.propertyAnimation(_645,_647,_640,_641,{"beforeBegin":function(){
h.setDisplay(_645,"block");
},"onEnd":function(){
h.setDisplay(_63f,"block");
_645.parentNode.removeChild(_645);
}});
if(_642){
anim.connect("onEnd",function(){
_642(_63f,anim);
});
}
return anim;
};
dojo.lfx.html.implode=function(_64a,end,_64c,_64d,_64e){
var h=dojo.html;
_64a=dojo.byId(_64a);
end=dojo.byId(end);
var _650=dojo.html.toCoordinateObject(_64a,true);
var _651=dojo.html.toCoordinateObject(end,true);
var _652=document.createElement("div");
dojo.html.copyStyle(_652,_64a);
dojo.html.setOpacity(_652,0.3);
with(_652.style){
position="absolute";
display="none";
backgroundColor=h.getStyle(_64a,"background-color").toLowerCase();
}
dojo.body().appendChild(_652);
var _653={opacity:{start:1,end:0.5}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_653[type]={start:_650[type],end:_651[type]};
});
var anim=new dojo.lfx.propertyAnimation(_652,_653,_64c,_64d,{"beforeBegin":function(){
dojo.html.hide(_64a);
dojo.html.show(_652);
},"onEnd":function(){
_652.parentNode.removeChild(_652);
}});
if(_64e){
anim.connect("onEnd",function(){
_64e(_64a,anim);
});
}
return anim;
};
dojo.lfx.html.highlight=function(_656,_657,_658,_659,_65a){
_656=dojo.lfx.html._byId(_656);
var _65b=[];
dojo.lang.forEach(_656,function(node){
var _65d=dojo.html.getBackgroundColor(node);
var bg=dojo.html.getStyle(node,"background-color").toLowerCase();
var _65f=dojo.html.getStyle(node,"background-image");
var _660=(bg=="transparent"||bg=="rgba(0, 0, 0, 0)");
while(_65d.length>3){
_65d.pop();
}
var rgb=new dojo.gfx.color.Color(_657);
var _662=new dojo.gfx.color.Color(_65d);
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:rgb,end:_662}},_658,_659,{"beforeBegin":function(){
if(_65f){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+rgb.toRgb().join(",")+")";
},"onEnd":function(){
if(_65f){
node.style.backgroundImage=_65f;
}
if(_660){
node.style.backgroundColor="transparent";
}
if(_65a){
_65a(node,anim);
}
}});
_65b.push(anim);
});
return dojo.lfx.combine(_65b);
};
dojo.lfx.html.unhighlight=function(_664,_665,_666,_667,_668){
_664=dojo.lfx.html._byId(_664);
var _669=[];
dojo.lang.forEach(_664,function(node){
var _66b=new dojo.gfx.color.Color(dojo.html.getBackgroundColor(node));
var rgb=new dojo.gfx.color.Color(_665);
var _66d=dojo.html.getStyle(node,"background-image");
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:_66b,end:rgb}},_666,_667,{"beforeBegin":function(){
if(_66d){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+_66b.toRgb().join(",")+")";
},"onEnd":function(){
if(_668){
_668(node,anim);
}
}});
_669.push(anim);
});
return dojo.lfx.combine(_669);
};
dojo.lang.mixin(dojo.lfx,dojo.lfx.html);
dojo.provide("dojo.lfx.*");

