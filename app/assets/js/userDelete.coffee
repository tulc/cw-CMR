$ ->
  $('#confirm-delete').on('show.bs.modal', (event) ->
    $('#formDelete').attr('action',$(event.relatedTarget).data('href'))
    $(this).find('.modal-body').append('<p>Do you want to delete user <strong>'+$(event.relatedTarget).data('username')+'</strong> ?</p>')
  )
  $('#confirm-delete').on('hide.bs.modal', (event) ->
    $('#formDelete').removeAttr('action')
    $(this).find('.modal-body').empty()
  )


