$ ->
  $('#datepicker').datepicker({
    format: "yyyy-mm-dd"
    todayBtn: "linked"
    clearBtn: true
    daysOfWeekHighlighted: "1"
    calendarWeeks: true
    todayHighlight: true
  });

  $('#btnViewReport').click () ->
    $('#redirectIndex').removeAttr('action')
    btnViewReport = $(this)
    btnViewReport.button('loading')
    fromDate = $('#fromDate').val()
    toDate = $('#toDate').val()
    if(fromDate == '')
      btnViewReport.button('reset')
      $('#fromDate').datepicker('show')
      return
    if(toDate == '')
      btnViewReport.button('reset')
      $('#toDate').datepicker('show')
      return
    $('#redirectIndex').submit()
    btnViewReport.button('reset')
