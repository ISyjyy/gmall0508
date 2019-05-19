
setTimeout(function() {
  var cha = 4 * 1000 * 3600;

  function time() {
    cha = cha - 1000;
    var hours = parseInt(cha / 1000 / 3600) % 24;
    var minutes = parseInt(cha / 1000 / 60) % 60;
    var seconds = parseInt(cha / 1000) % 60;
    if(hours < 10) {
      hours = "0" + hours
    }
    if(minutes < 10) {
      minutes = "0" + minutes
    }
    if(seconds < 10) {
      seconds = "0" + seconds
    }
    $(".section_second_header_right_hours").html(hours);
    $(".section_second_header_right_minutes").html(minutes);
    $(".section_second_header_right_second").html(seconds);
  }
  setInterval(time, 1000)
}, 1)
