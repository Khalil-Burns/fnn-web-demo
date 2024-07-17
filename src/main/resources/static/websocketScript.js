let stompClient = null;
let id = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/stomp-endpoint');
    stompClient = Stomp.over(socket);
    id = new Date().getTime();
    stompClient.connect({}, function (frame) {
        // setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe(`/canvas/${id}`, function (greeting) {
        // stompClient.subscribe(`/canvas/1`, function (greeting) {
            // showGreeting(JSON.parse(greeting.body));
            console.log(JSON.parse(greeting.body)['message'])
            greeting = JSON.parse(JSON.parse(greeting.body)['message'].replace(/'/g, '"'));
            $('#0-display').html(parseFloat(greeting['0']).toFixed(3));
            $('#1-display').html(parseFloat(greeting['1']).toFixed(3));
            $('#2-display').html(parseFloat(greeting['2']).toFixed(3));
            $('#3-display').html(parseFloat(greeting['3']).toFixed(3));
            $('#4-display').html(parseFloat(greeting['4']).toFixed(3));
            $('#5-display').html(parseFloat(greeting['5']).toFixed(3));
            $('#6-display').html(parseFloat(greeting['6']).toFixed(3));
            $('#7-display').html(parseFloat(greeting['7']).toFixed(3));
            $('#8-display').html(parseFloat(greeting['8']).toFixed(3));
            $('#9-display').html(parseFloat(greeting['9']).toFixed(3));
            $('#guess-display').html(greeting['MAX']);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendPixelArray() {
    stompClient.send(`/app/hello/${id}`, {}, JSON.stringify({"name":JSON.stringify(pixelData)}));
    // stompClient.send(`/app/hello/1`, {}, JSON.stringify({"name":JSON.stringify(pixelData)}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message.message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});