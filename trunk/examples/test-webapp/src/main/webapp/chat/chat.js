

var room = 
{
  _last: "",
  _username: null,
  
  join: function(name)
  {
    if (name == null || name.length==0 )
    {
      alert('Please enter a username!');
    }
    else
    {
       this._username=name;
       $('join').className='hidden';
       $('joined').className='';
       $('phrase').focus();
       Behaviour.apply();
       ajax.sendMessage('join', room._username);
    }
  },
  
  leave: function()
  {
       // switch the input form
       $('join').className='';
       $('joined').className='hidden';
       $('username').focus();
       Behaviour.apply();
       ajax.sendMessage('leave',room._username);
       room._username=null;
  },
  
  chat: function(text)
  {
    if (text != null && text.length>0 )
    {
        ajax.sendMessage('chat',text);
    }
  },
  
  _chat: function(message)
  {
     var divChat=document.getElementById("chat");
     var from=message.getAttribute('from');
     var special=message.getAttribute('alert');
     var text=message.childNodes[0].data;
     if ( special!='true' && from == room._last )
         from="...";
     else
     {
         room._last=from;
         from+=":";
     }
     
     
     
     var parentElement = divChat;
     if (special=='true')
     {
       var span = document.createElement("span");
       span.className="alert";
       divChat.appendChild(span);
       parentElement=span;
     }
     var spanFrom = document.createElement("span");
     spanFrom.className="from";
     spanFrom.innerHTML=from+"&nbsp;";
     var spanText = document.createElement("span");
     spanText.className="text";
     spanText.innerHTML=text;
     var lineBreak = document.createElement("br");
     parentElement.appendChild(spanFrom);
     parentElement.appendChild(spanText);
     divChat.appendChild(lineBreak);
     divChat.scrollTop = divChat.scrollHeight - divChat.clientHeight;     
  },
   
  _members: function(message)
  {   
    try
    {
        var divMembers = document.getElementById("members");
        
        if(message.xml != undefined)
        {
            var spanElem = document.createElement("<span>");
            spanElem.innerHTML = ajax.getContentAsString(message);
            if(divMembers.innerText != "")
            {
                divMembers.innerText="";
            }
            divMembers.appendChild(spanElem);
            
        }
        else
        {
            divMembers.innerHTML = ajax.getContentAsString(message);
        }
    }
    catch(e)
    {
        alert("_members " + e);
    }
  },
      
  _joined: function(message)
  {    
     var from=message.getAttribute('from');
     var chat=$('chat');
     chat.innerHTML += "<span class=\"alert\"><span class=\"from\">"+from+"&nbsp;</span><span class=\"text\">has joined the room!</span></span><br/>";
     chat.scrollTop = chat.scrollHeight - chat.clientHeight;     
  },
  
  _left: function(message)
  {    
     var from=message.getAttribute('from');
     var chat=$('chat');
     chat.innerHTML += "<span class=\"alert\"><span class=\"from\">"+from+"&nbsp;</span><span class=\"text\">has left the room!</span></span><br/>";
     chat.scrollTop = chat.scrollHeight - chat.clientHeight;     
  },
  
  _poll: function(first)
  {
     if (first ||  $('join').className=='hidden' && $('joined').className=='hidden')
     {
       ajax.addListener('chat',room._chat);
       ajax.addListener('joined',room._joined);
       ajax.addListener('left',room._left);
       ajax.addListener('members',room._members);
       $('join').className='';
       $('joined').className='hidden';
       $('username').focus();
      Behaviour.apply();
     }
  }
};

ajax.addPollHandler(room._poll);

var chatBehaviours = 
{ 
  '#username' : function(element)
  {
    element.setAttribute("autocomplete","OFF"); 
    element.onkeyup = function(ev)
    {          
        var keyc=EvUtil.getKeyCode(ev);
        if (keyc==13 || keyc==10)
        {
          room.join($F('username'));
	  return false;
	}
	return true;
    } 
  },
  
  '#joinB' : function(element)
  {
    element.onclick = function(event)
    {
      room.join($F('username'));
      return false;
    }
  },
  
  '#phrase' : function(element)
  {
    element.setAttribute("autocomplete","OFF");
    element.onkeyup = function(ev)
    {   
        var keyc=EvUtil.getKeyCode(ev);
        if (keyc==13 || keyc==10)
        {
          room.chat($F('phrase'));
          $('phrase').value='';
	  return false;
	}
	return true;
    }
  },
  
  '#sendB' : function(element)
  {
    element.onclick = function(event)
    {
      room.chat($F('phrase'));
      $('phrase').value='';
      return false;
    }
  },
  
  
  '#leaveB' : function(element)
  {
    element.onclick = function()
    {
      room.leave();
      return false;
    }
  }
};

Behaviour.register(chatBehaviours); 


