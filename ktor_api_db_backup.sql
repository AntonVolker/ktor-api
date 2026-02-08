--
-- PostgreSQL database dump
--

\restrict j1lqrDMqTxdME98Dg8dw4Ka2bObV3YCHXhfnNIt7diGgZBslu73fF9ZoQldTP59

-- Dumped from database version 16.11 (Homebrew)
-- Dumped by pg_dump version 16.11 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ev_charging_stations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ev_charging_stations (
    id uuid NOT NULL,
    location_id uuid NOT NULL,
    charger_type character varying(50) NOT NULL,
    num_chargers integer NOT NULL,
    charging_speed_kw double precision NOT NULL,
    is_fast_charging boolean NOT NULL,
    cost_per_kwh double precision,
    is_operational boolean NOT NULL
);


ALTER TABLE public.ev_charging_stations OWNER TO postgres;

--
-- Name: locations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.locations (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    address character varying(255) NOT NULL,
    city character varying(255) NOT NULL,
    state character varying(255),
    zip_code character varying(20),
    country character varying(255) NOT NULL,
    type character varying(50) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


ALTER TABLE public.locations OWNER TO postgres;

--
-- Name: parking_spaces; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parking_spaces (
    id uuid NOT NULL,
    location_id uuid NOT NULL,
    total_spaces integer NOT NULL,
    available_spaces integer NOT NULL,
    hourly_rate double precision,
    max_duration_hours integer,
    is_handicap_accessible boolean NOT NULL,
    is_covered boolean NOT NULL
);


ALTER TABLE public.parking_spaces OWNER TO postgres;

--
-- Data for Name: ev_charging_stations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ev_charging_stations (id, location_id, charger_type, num_chargers, charging_speed_kw, is_fast_charging, cost_per_kwh, is_operational) FROM stdin;
e16f7367-eeb5-4521-bbd1-c6fa5079ffcd	6b34fbb3-f09a-414e-9b51-895ffdf8df14	CCS_COMBO_2_DC	4	150	t	0.35	t
83eeacab-98bc-4ca2-bccd-eb9a655f1d4f	40d0a711-8677-4443-b9af-931f34f2b28c	CCS_COMBO_2_DC	4	150	t	0.35	t
\.


--
-- Data for Name: locations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.locations (id, name, description, latitude, longitude, address, city, state, zip_code, country, type, created_at, updated_at) FROM stdin;
6b34fbb3-f09a-414e-9b51-895ffdf8df14	Downtown Charging Hub	Fast chargers near City Hall	34.0522	-118.2437	200 N Spring St	Los Angeles	CA	90012	USA	EV_CHARGING_STATION	2026-02-08 00:03:28.685056	2026-02-08 00:03:28.685074
40d0a711-8677-4443-b9af-931f34f2b28c	Downtown Charging Hub	Fast chargers near City Hall	34.0522	-118.2437	200 N Spring St	Los Angeles	CA	90012	USA	EV_CHARGING_STATION	2026-02-08 00:30:50.771032	2026-02-08 00:30:50.771042
40babb8a-ad26-4ec7-ac90-406016b2a387	City Hall Parking Garage	Underground parking available 24/7	34.0522	-118.2437	200 N Spring St	Los Angeles	CA	90012	USA	PARKING_SPACE	2026-02-08 00:32:50.041222	2026-02-08 00:32:50.041237
\.


--
-- Data for Name: parking_spaces; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parking_spaces (id, location_id, total_spaces, available_spaces, hourly_rate, max_duration_hours, is_handicap_accessible, is_covered) FROM stdin;
e1825f8f-4ed9-410d-b430-1cddd9564945	40babb8a-ad26-4ec7-ac90-406016b2a387	250	180	2.75	8	t	t
\.


--
-- Name: ev_charging_stations ev_charging_stations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ev_charging_stations
    ADD CONSTRAINT ev_charging_stations_pkey PRIMARY KEY (id);


--
-- Name: locations locations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);


--
-- Name: parking_spaces parking_spaces_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parking_spaces
    ADD CONSTRAINT parking_spaces_pkey PRIMARY KEY (id);


--
-- Name: ev_charging_stations fk_ev_charging_stations_location_id__id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ev_charging_stations
    ADD CONSTRAINT fk_ev_charging_stations_location_id__id FOREIGN KEY (location_id) REFERENCES public.locations(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: parking_spaces fk_parking_spaces_location_id__id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parking_spaces
    ADD CONSTRAINT fk_parking_spaces_location_id__id FOREIGN KEY (location_id) REFERENCES public.locations(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;


--
-- Name: DEFAULT PRIVILEGES FOR FUNCTIONS; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO postgres;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO postgres;


--
-- PostgreSQL database dump complete
--

\unrestrict j1lqrDMqTxdME98Dg8dw4Ka2bObV3YCHXhfnNIt7diGgZBslu73fF9ZoQldTP59

