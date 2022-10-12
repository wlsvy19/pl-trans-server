-- ip : 대상 서버 IP
-- file type : ACCESS - access log file, WAS - was log, EB - ebrother log, ERR - error log file.
-- send_type : R - real time, B - batch
-- isfinish : Y - end, N - no
-- file_server : 파일의 server 위치
-- file_client : 

create table rptrans_filelog (
	seq_filelog	int generated always as identity
	,	client_ip	varchar(20)
	, file_server	varchar(512)
	, file_client varchar(512)
	, file_ymd	char(8)
	, file_hms	char(6)
	, file_nm		varchar(512)
	, file_size	int
	, file_lines	int
	, file_type	varchar(10)
	, send_type	char(1)
	, isfinish	char(1)
	, admindate	char(14)
);

create unique index idx_translog on rptrans_filelog ( client_ip, file_server, admindate );

-- 최종 파일 전송 로그
create table rptrans_filehist (
	seq_filehist	int generated always as identity
	, seq_filelog	int
	,	client_ip	varchar(20)
	, file_server	varchar(512)
	, file_client varchar(512)
	, file_ymd	char(8)
	, file_hms	char(6)
	, file_nm		varchar(512)
	, file_size	int
	, file_lines	int
	, file_type	varchar(10)
	, send_type	char(1)
	, admindate	char(14)
);

create unique index idx_transhist on rptrans_filehist ( client_ip, file_server );


drop table rptrans_parselog;


create table rptrans_parselog (
	seq_parselog	int generated always as identity
	, seq_filehist	int
	, trans_file	varchar(512)
	, parse_file	varchar(512)
	, parse_type	varchar(10)
	, parse_size		int
	, parse_lines int
	, parse_lierr int
	, admindate	char(14)
);

create unique index idx_parselog on rptrans_parselog ( seq_filehist, parse_file );

drop table rptrans_parsehist;


create table rptrans_parsehist (
	seq_parsehist	int generated always as identity
	, seq_filehist	int
	, trans_file	varchar(512)
	, parse_file	varchar(512)
	, parse_type	varchar(10)
	, parse_size	int
	, parse_lines int
	, parse_lierr int
	, admindate	char(14)
);

create unique index idx_parsehist on rptrans_parsehist ( seq_filehist, parse_file );



