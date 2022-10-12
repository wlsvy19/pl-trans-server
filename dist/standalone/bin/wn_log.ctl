load data
append into table WN_LOG
FIELDS TERMINATED BY "$^" OPTIONALLY ENCLOSED BY '"'
( 
	idanon
	, idvisit
	, sitecd
	, url
	, ip
	, userid
	, agent char(2000) " substrb( replace(:agent, 'HANA_DEVICE=[', ''), 1, 2000) "
	, isfirst
	, ref  char(2000) " substrb ( :ref, 1, 2000) "
	, cookie
	, method
	, admindate
	, query char(4000) "substrb(:query,1,4000)"
	, seqlog "seqwnlog.nextval "
)
	
