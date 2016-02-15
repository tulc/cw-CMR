$ ->
  $('#navbarCollapse .nav li a').click ->
    $('#navbarCollapse .nav li.active').removeClass('active')
    $(this).parent('li').addClass('active')