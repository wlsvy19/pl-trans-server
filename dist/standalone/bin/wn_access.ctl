load data
append into table WN_ACCLOG
FIELDS TERMINATED BY "$^" OPTIONALLY ENCLOSED BY '"'
( 
	domain	"FNCCGETDELIMITDATA(:domain, '-', 1 )"
	, ymd
	, hms
	, ip
	, url  "substrb(:url, 1, 512 )"

	, tmp1	filler
	, tmp2  filler
	, tmp3  filler
	, res_stat
	, res_size "replace(:res_size, '-', '0' )"

)
	
