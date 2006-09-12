

// AJAX  handler
var ajax = 
{
  poll: true,
  _first: true,
  _pollEvent: function(first) {},
  _handlers: new Array(),
  
  _messages:0,
  _messageQueue: '',
  _queueMessages: false,
  
  _messageHandler: function(request) 
  {
    var qm=ajax._queueMessages;
    ajax._queueMessages=true;
    try
    {
      if (request.status == 200)
      {
        var response = request.responseXML.getElementsByTagName("ajax-response");
        if (response != null && response.length == 1)
        {
          for ( var i = 0 ; i < response[0].childNodes.length ; i++ ) 
          {
            var responseElement = response[0].childNodes[i];
	    
            // only process nodes of type element.....
            if ( responseElement.nodeType != 1 )
              continue;

            var id   = responseElement.getAttribute('id');
            
            var handler = ajax._handlers[id];
            if (handler!=null)
            {
              for (var j = 0; j < responseElement.childNodes.length; j++) 
              {
                var child = responseElement.childNodes[j]
                if (child.nodeType == 1) 
                {
                  handler(child);
                }
        	      }
            }
          }
        }
      }
    }
    catch(e)
    {
      alert(e);
    }
    ajax._queueMessages=qm;
    
    if (!ajax._queueMessages && ajax._messages>0)
    {
      var body = ajax._messageQueue;
      ajax._messageQueue='';
      ajax._messages=0;
      new Ajax.Request('.', { method: 'post', onSuccess: ajax._pollHandler, postBody: body }); 
    }
  },
  
  _pollHandler: function(request) 
  {
    ajax._queueMessages=true;
    try
    {
      ajax._messageHandler(request);
      ajax._pollEvent(ajax._first);
      ajax._first=false;
    }
    catch(e)
    {
        alert(e);
    }
    
    ajax._queueMessages=false;
    
    if (ajax._messages==0)
    {
      if (ajax.poll)
        new Ajax.Request('.', { method: 'get', parameters: 'ajax=poll&message=poll', onSuccess: ajax._pollHandler }); 
    }
    else
    {
      var body = ajax._messageQueue+'&ajax=poll&message=poll';
      ajax._messageQueue='';
      ajax._messages=0;
      new Ajax.Request('.', { method: 'post', onSuccess: ajax._pollHandler, postBody: body }); 
    }
  },
  
  addPollHandler : function(func)
  {
    var old = ajax._pollEvent;
    ajax._pollEvent = function(first) 
    {
      old(first);
      func(first);
    }
  },
  
  // Listen on a channel or topic.   handler must be a function taking a message arguement
  addListener : function(id,handler)
  {   
    ajax._handlers[id]=handler;
  },
  
  // remove Listener from channel or topic.  
  removeListener : function(id)
  {   
    ajax._handlers[id]=null;
  },
  
  sendMessage : function(destination,message)
  {
    message=message.replace('%','%25');
    message=message.replace('&','%26');
    message=message.replace('=','%3D');
    if (ajax._queueMessages)
    {
      ajax._messageQueue+=(ajax._messages==0?'ajax=':'&ajax=')+destination+'&message='+message;
      ajax._messages++;
    }
    else
    {
      new Ajax.Request('.', { method: 'post', postBody: 'ajax='+destination+'&message='+message,onSuccess: ajax._messageHandler});
    }
  },
  
  _startPolling : function()
  {
    if (ajax.poll)
      new Ajax.Request('.', { method: 'get', parameters: 'ajax=poll&message=poll&timeout=0', onSuccess: ajax._pollHandler });
  },
  
  getContentAsString: function( parentNode ) {
      return parentNode.xml != undefined ?
         ajax._getContentAsStringIE(parentNode) :
         ajax._getContentAsStringMozilla(parentNode);
  },

  _getContentAsStringIE: function(parentNode) {
     var contentStr = "";
     for ( var i = 0 ; i < parentNode.childNodes.length ; i++ ) {
         var n = parentNode.childNodes[i];
         if (n.nodeType == 4) {
             contentStr += n.nodeValue;
         }
         else {
           contentStr += n.xml;
       }
     }
     return contentStr;
  },

  _getContentAsStringMozilla: function(parentNode) {
     var xmlSerializer = new XMLSerializer();
     var contentStr = "";
     for ( var i = 0 ; i < parentNode.childNodes.length ; i++ ) {
          var n = parentNode.childNodes[i];
          if (n.nodeType == 4) { // CDATA node
              contentStr += n.nodeValue;
          }
          else {
            contentStr += xmlSerializer.serializeToString(n);
        }
     }
     return contentStr;
  }
  
};

var EvUtil =
{
    getKeyCode : function(ev)
    {
        var keyc;
        if (window.event)
            keyc=window.event.keyCode;
        else
            keyc=ev.keyCode;
        return keyc;
    }
};

Behaviour.addLoadEvent(ajax._startPolling);  

