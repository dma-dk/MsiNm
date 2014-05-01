
select msg from Message msg
where msg.status = 'ACTIVE'
and (
  msg.id in (select msi.id from NavwarnMessage msi where msi.cancellationDate is null or msi.cancellationDate > current_timestamp )
  or
  msg.id in (select nm.id from NoticeMessage nm)
)
