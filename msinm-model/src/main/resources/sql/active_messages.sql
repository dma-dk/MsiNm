
select msg from Message msg
where msg.status = 'PUBLISHED'
and (msg.cancellationDate is null or msg.cancellationDate > current_timestamp )
