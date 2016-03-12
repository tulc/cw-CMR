$ ->
  Array.prototype.nonExist = (element) ->
    return false for item in this when item == element
    return true

  buildChartSeries = (listData) ->
    {'name': item.facultyName, 'data':[item.countCompletedCMR]} for item in listData

  $('#datepicker').datepicker({
    format: "yyyy-mm-dd"
    todayBtn: "linked"
    clearBtn: true
    daysOfWeekHighlighted: "1"
    calendarWeeks: true
    todayHighlight: true
  });

  $('#btnViewReport').click () ->
    btnViewReport = $(this)
    btnViewReport.button('loading')
    formDate = $('#formDate').val()
    endDate = $('#endDate').val()
    if(formDate == '')
      btnViewReport.button('reset')
      $('#startDate').datepicker('show')
      return
    if(endDate == '')
      btnViewReport.button('reset')
      $('#endDate').datepicker('show')
      return
    $.ajax url: '/systemStatisticReports/' + formDate + '/' + endDate,
    type: 'GET',
    success: (data) ->
      chartData = []
      chartCategories = []
      $.each(data, (index,value) ->
        chartCategories.push(value.academicSeasonName) if chartCategories.nonExist(value.academicSeasonName)
      )
      $.each(chartCategories, (index, category) ->
        chartData.push('category' : category, 'series' : buildChartSeries(data,category))
      )
      abc = buildChartSeries(data)
      buildChart(abc)
      console.log(chartData)
      console.log(abc)
    btnViewReport.button('reset')
