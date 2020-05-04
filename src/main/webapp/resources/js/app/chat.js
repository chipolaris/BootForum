var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
}

function connect() {
    var socket = new SockJS('/BootForum/chat-connect');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function postMessage(channelId) {
	
	var messageText = $("#messageText" + channelId).val();
	
	if((!messageText || /^\s*$/.test(messageText))) {
		return;
	}
	
    stompClient.send("/BootForum/chat/postMessage/" + channelId, {}, JSON.stringify({'messageText': messageText.trim()}));
    $("#messageText" + channelId).val(''); // clear the input field
    //$("#counterMessage" + channelId).text(origCounterMessageText);
}

function postImage(channelId) {
	
	var imageBase64 = $('#imagePreview').attr('src');
	
    stompClient.send("/BootForum/chat/postImage/" + channelId, {}, JSON.stringify({'imageBase64': imageBase64}));
}

function showMessage(channelId, messageObj) {
	
	if(messageObj.type == 'userMessage') {
		showUserMessage(channelId, messageObj);
	}
	else if(messageObj.type == 'userImage') {
		showUserImage(channelId, messageObj);
	}
	else if(messageObj.type == 'channelMessage') {
		showChannelMessage(channelId, messageObj);
	}
    
    // scroll to bottom of chat window with animation, duration 400 miliseconds
    $("#conversationDiv" + channelId).animate({ scrollTop: $('#conversationDiv' + channelId).prop("scrollHeight")}, 400);
}

function showChannelMessage(channelId, messageObj) {
	 $("#channelContent" + channelId).append("<tr><td colspan=2 class='w3-large'><span class='w3-text-orange'>" 
			 + messageObj.message + "</span> <span class='w3-text-blue'> - " 
			 + new Date(messageObj.timeMillis).toLocaleTimeString() 
			 + "</span></td></tr>");
}

function showUserMessage(channelId, messageObj) {
	
	var avatar = "";
	
	if(messageObj.avatarExists) {
		avatar = "<img src='../avatar/" + messageObj.username 
			+ "' class='chatAvatarImage w3-circle' title='" + messageObj.username + "'/>";
	}
	else {
		avatar = "<span class='chatAvatarSpan w3-circle' style='background-color:" 
	    	+ stringToColor(messageObj.username) + "' title='" + messageObj.username + "'>"
	    	+ messageObj.username.substring(0,3) + "</span>";
	}
	
    $("#channelContent" + channelId).append("<tr><td><p>" + avatar
    		+ "</p></td><td><p><b>" + messageObj.username + "</b> <span class='w3-text-blue'> - <b>" 
    		+ new Date(messageObj.timeMillis).toLocaleTimeString() 
    		+ "</b></span></p><p>" + messageObj.content 
    		+ "</p></td></tr>");
}

function showUserImage(channelId, messageObj) {
	
	var avatar = "";
	
	if(messageObj.avatarExists) {
		avatar = "<img src='../avatar/" + messageObj.username 
			+ "' class='chatAvatarImage w3-circle' title='" + messageObj.username + "'/>";
	}
	else {
		avatar = "<span class='chatAvatarSpan w3-circle' style='background-color:" 
	    	+ stringToColor(messageObj.username) + "' title='" + messageObj.username + "'>"
	    	+ messageObj.username.substring(0,3) + "</span>";
	}
	
    $("#channelContent" + channelId).append("<tr><td><p>" + avatar
    		+ "</p></td><td><p><b>" + messageObj.username + "</b> <span class='w3-text-blue'> - <b>" 
    		+ new Date(messageObj.timeMillis).toLocaleTimeString() 
    		+ "</b></span></p><div><img style='width:100%;max-width:450px;height:auto;' src='" 
    		+ messageObj.content + "' alt='Image'/></div></td></tr>");
}

/* 
 * Image Preview 
 * references: 
 * 		https://stackoverflow.com/questions/4459379/preview-an-image-before-it-is-uploaded
 * 		https://developer.mozilla.org/en-US/docs/Web/API/FileReader/readAsDataURL
 */
function readURL(input) {
	if (input.files && input.files[0]) {
		
		const reader = new FileReader();
    
		reader.onload = function(e) {
			$('#imagePreview').attr('src', e.target.result);
			$('#imagePreviewModal').show();
		}
    
		reader.readAsDataURL(input.files[0]); // convert to base64 string
	}
}
// keep track of the selectedChannel for image preview and image post
var selectedChannel = '';

var origCounterMessageText = '';

var channelSubscriptions = new Map();

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    
    $( ".toggleChanel" ).click(function(){
    	
    	// retrieve channelId from the button, note: use lowercase
    	var channelId = this.dataset.channelid;
    	
    	// toggle channel div with animation (duration 400)
    	$('#channel' + channelId).toggle(400);
    });
    
    $( ".enterChannel" ).click(function(){
    	
    	// retrieve channelId from the button, note: use lowercase
    	var channelId = this.dataset.channelid;
    	
    	var channelSubscription = stompClient.subscribe("/channel/" + channelId, function (message) {
            showMessage(channelId, JSON.parse(message.body));
        });
    	
    	// store the subscription in the channelSubscriptions map
    	channelSubscriptions.set("channel" + channelId, channelSubscription);
    	
    	// disable enter button
    	$(this).prop("disabled", true);
    	
    	// enable leave button
    	$('#leaveChannel' + channelId).prop("disabled", false);
    	
    	// enable post message form fieldset
    	$('#fieldSet' + channelId).prop("disabled", false);
    });
    
    $( ".leaveChannel" ).click(function(){
    	
    	// retrieve channelId from the button, note: use lowercase
    	var channelId = this.dataset.channelid;
    	
    	var channelSubscription = channelSubscriptions.get("channel" + channelId);
    	channelSubscription.unsubscribe();
    	// stompClient.unsubscribe("/channel/" + channelId);
    	
    	// disable leave button
    	$(this).prop("disabled", true);
    	
    	// enable leave button
    	$('#enterChannel' + channelId).prop("disabled", false);
    	
    	// disable post message form fieldset
    	$('#fieldSet' + channelId).prop("disabled", true);
    	
    	// clear channel content
    	$("#channelContent" + channelId).html("");
    });
    
    $( ".postMessage" ).click(function(){
    	
    	// retrieve channelId from the button, note: use lowercase
    	var channelId = this.dataset.channelid;
    	
    	postMessage(channelId);
    });    
    
    // keep the original messageCounter text to reset later when user post/submit a message
    //origCounterMessageText = $("#counterMessage").text();

    // callback image for when imageInput file input is selected
    $(".imageInput").change(function(e) {
    	
    	selectedChannel = this.dataset.channelid;
    	const file = e.target.files[0];
    	if(!file.type.startsWith('image/')) {
    		alert('not valid image');
    		return;
    	}
    	$('#imagePreviewHeader').text('Image Preview: ' + file.name.split('\\').pop());
    	readURL(this);
    });
    
    $("#postImageButton").click(function() {
    	
    	$('#imagePreviewModal').hide();
    	postImage(selectedChannel);
    });
});